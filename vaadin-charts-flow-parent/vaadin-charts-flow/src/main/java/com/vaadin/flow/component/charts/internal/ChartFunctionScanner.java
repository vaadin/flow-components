/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.frontendtools.internal.asm.ClassReader;
import com.vaadin.frontendtools.internal.asm.ClassVisitor;
import com.vaadin.frontendtools.internal.asm.Handle;
import com.vaadin.frontendtools.internal.asm.Label;
import com.vaadin.frontendtools.internal.asm.MethodVisitor;
import com.vaadin.frontendtools.internal.asm.Opcodes;

/**
 * Finds the JavaScript function sources that application code passes as
 * constants to the {@code _fn_} setters of the Charts model, such as
 * {@code Tooltip.setFormatter(String)}.
 * <p>
 * A source is found when the argument of the setter call is a constant in the
 * bytecode: a string literal, a concatenation of literals or a
 * {@code static final} constant (javac folds all three into one constant), or a
 * local variable that was assigned such a constant. A source that is built at
 * runtime is not found.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class ChartFunctionScanner {

    static final String MODEL_PACKAGE = "com/vaadin/flow/component/charts/model/";

    private static final String STRING_SETTER_DESCRIPTOR = "(Ljava/lang/String;)V";

    private static final int ASM_API = Opcodes.ASM9;

    private final Function<String, Class<?>> classLoader;

    private final Map<String, Boolean> functionSetters = new ConcurrentHashMap<>();

    /**
     * Creates a scanner.
     *
     * @param classLoader
     *            loads a Charts model class by its binary name, to check
     *            whether a setter writes a {@code _fn_} field
     */
    public ChartFunctionScanner(Function<String, Class<?>> classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Scans every class file in the given class path directories.
     *
     * @param directories
     *            the class path roots to scan, such as {@code target/classes}
     * @return the function sources, sorted
     */
    public Set<String> scanDirectories(Iterable<Path> directories) {
        Set<String> sources = new TreeSet<>();
        for (Path directory : directories) {
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(directory)) {
                files.filter(file -> file.toString().endsWith(".class"))
                        .forEach(file -> {
                            try (InputStream in = Files.newInputStream(file)) {
                                sources.addAll(scanClass(in));
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return sources;
    }

    /**
     * Scans the class path directories that the given class loader exposes.
     * Classes inside jar files are not scanned.
     *
     * @param loader
     *            the class loader of the application
     * @return the function sources, sorted
     */
    public Set<String> scanClassLoader(ClassLoader loader) {
        Set<Path> directories = new TreeSet<>();
        try {
            for (URL url : Collections.list(loader.getResources(""))) {
                if ("file".equals(url.getProtocol())) {
                    directories.add(Path.of(url.toURI()));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LoggerFactory.getLogger(ChartFunctionScanner.class)
                    .warn("Failed to list the class path directories", e);
        }
        return scanDirectories(directories);
    }

    /**
     * Scans one class file.
     *
     * @param classFile
     *            the bytes of the class file
     * @return the function sources the class passes to {@code _fn_} setters
     * @throws IOException
     *             if the class file cannot be read
     */
    public Set<String> scanClass(InputStream classFile) throws IOException {
        Set<String> sources = new TreeSet<>();
        new ClassReader(classFile).accept(new ClassVisitor(ASM_API) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                return new ConstantTrackingVisitor(sources);
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return sources;
    }

    private boolean isFunctionSetter(String owner, String name,
            String descriptor) {
        if (!owner.startsWith(MODEL_PACKAGE)
                || !STRING_SETTER_DESCRIPTOR.equals(descriptor)
                || name.length() < 4 || !name.startsWith("set")) {
            return false;
        }
        return functionSetters.computeIfAbsent(owner + "." + name, key -> {
            String fieldName = "_fn_" + Character.toLowerCase(name.charAt(3))
                    + name.substring(4);
            Class<?> type;
            try {
                type = classLoader.apply(owner.replace('/', '.'));
            } catch (RuntimeException | LinkageError e) {
                return false;
            }
            for (; type != null; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Remembers the string constant that is on top of the operand stack, and
     * the string constants stored in local variables, so that the argument of a
     * setter call can be resolved. Any other instruction forgets the constant
     * on the stack.
     */
    private final class ConstantTrackingVisitor extends MethodVisitor {

        private final Set<String> sources;

        private final Map<Integer, String> constantLocals = new HashMap<>();

        private String constantOnStack;

        private ConstantTrackingVisitor(Set<String> sources) {
            super(ASM_API);
            this.sources = sources;
        }

        @Override
        public void visitLdcInsn(Object value) {
            constantOnStack = value instanceof String string ? string : null;
        }

        @Override
        public void visitVarInsn(int opcode, int varIndex) {
            if (opcode == Opcodes.ASTORE) {
                if (constantOnStack != null) {
                    constantLocals.put(varIndex, constantOnStack);
                } else {
                    constantLocals.remove(varIndex);
                }
                constantOnStack = null;
            } else if (opcode == Opcodes.ALOAD) {
                constantOnStack = constantLocals.get(varIndex);
            } else {
                constantOnStack = null;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                String descriptor, boolean isInterface) {
            if (constantOnStack != null
                    && isFunctionSetter(owner, name, descriptor)) {
                sources.add(constantOnStack);
            }
            constantOnStack = null;
        }

        @Override
        public void visitInsn(int opcode) {
            constantOnStack = null;
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            constantOnStack = null;
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            constantOnStack = null;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name,
                String descriptor) {
            constantOnStack = null;
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor,
                Handle bootstrapMethodHandle,
                Object... bootstrapMethodArguments) {
            constantOnStack = null;
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            constantOnStack = null;
        }

        @Override
        public void visitLabel(Label label) {
            // A jump can reach this point with other values on the stack and
            // in the locals
            constantOnStack = null;
            constantLocals.clear();
        }

        @Override
        public void visitIincInsn(int varIndex, int increment) {
            constantOnStack = null;
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt,
                Label... labels) {
            constantOnStack = null;
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys,
                Label[] labels) {
            constantOnStack = null;
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor,
                int numDimensions) {
            constantOnStack = null;
        }
    }
}
