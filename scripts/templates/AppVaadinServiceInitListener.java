package com.vaadin;
import com.vaadin.flow.server.BootstrapListener;
import com.vaadin.flow.server.BootstrapPageResponse;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class AppVaadinServiceInitListener implements VaadinServiceInitListener  {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addBootstrapListener(new BootstrapListener() {
            @Override
            public void modifyBootstrapPage(BootstrapPageResponse response) {
                response.getDocument().head().append(
                        "  <script>\n" + 
                        "    const define = window.customElements.define;\n" + 
                        "    window.customElements.define = function(tag, elm) {\n" + 
                        "      try {\n" + 
                        "        define.call(this, tag, elm)\n" + 
                        "      } catch (error) {\n" + 
                        "        console.log(error);\n" + 
                        "      }\n" + 
                        "    }\n" + 
                        "  </script>");
            }
        });
    }
}
