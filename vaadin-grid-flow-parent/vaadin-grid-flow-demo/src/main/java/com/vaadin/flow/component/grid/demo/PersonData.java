package com.vaadin.flow.component.grid.demo;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PersonData {

    private final List<GridDemo.Person> PERSON_LIST = new ArrayList<>();

    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        PERSON_LIST.add(new GridDemo.Person(91, "Jack", "Giles", 50, new GridDemo.Address("12080", "Washington"), "127-949-238",
                GridDemo.MaritalStatus.Married, LocalDate.parse("02/09/1968", formatter)));
        PERSON_LIST.add(new GridDemo.Person(92, "Nathan", "Patterson", 20, new GridDemo.Address("12080", "Washington"), "107-922-137",
                GridDemo.MaritalStatus.Single, LocalDate.parse("05/10/1998", formatter)));
        PERSON_LIST.add(new GridDemo.Person(93, "Andrew", "Bauer", 30, new GridDemo.Address("12080", "New York"), "120-952-285",
                GridDemo.MaritalStatus.Single, LocalDate.parse("09/07/1988", formatter)));
        PERSON_LIST.add(new GridDemo.Person(94, "Mickael", "Blackwell", 68, new GridDemo.Address("12080", "Washington"), "127-942-237",
                GridDemo.MaritalStatus.Married, LocalDate.parse("05/10/1950", formatter)));
        PERSON_LIST.add(new GridDemo.Person(95, "Peter", "Buchanan", 38, new GridDemo.Address("93849", "New York"), "201-793-488",
                GridDemo.MaritalStatus.Single, LocalDate.parse("09/10/1980", formatter)));
        PERSON_LIST.add(new GridDemo.Person(96, "Samuel", "Lee", 53, new GridDemo.Address("86829", "New York"), "043-713-538",
                GridDemo.MaritalStatus.Married, LocalDate.parse("03/28/1965", formatter)));
        PERSON_LIST.add(new GridDemo.Person(97, "Anton", "Ross", 37, new GridDemo.Address("63521", "New York"), "150-813-6462",
                GridDemo.MaritalStatus.Single, LocalDate.parse("07/22/1981", formatter)));
        PERSON_LIST.add(new GridDemo.Person(98, "Aaron", "Atkinson", 18, new GridDemo.Address("25415", "Washington"), "321-679-8544",
                GridDemo.MaritalStatus.Single, LocalDate.parse("05/10/2000", formatter)));
        PERSON_LIST.add(new GridDemo.Person(99, "Jack", "Woodward", 28, new GridDemo.Address("95632", "New York"), "187-338-588",
                GridDemo.MaritalStatus.Married, LocalDate.parse("02/10/1990", formatter)));
        PERSON_LIST
                .add(new GridDemo.Person(100, "Elizabeth", "Blackwell", 11, new GridDemo.Address("12080", "Washington"), "127-942-237"));
        PERSON_LIST.add(new GridDemo.Person(101, "Mia", "Buchanan", 3, new GridDemo.Address("93849", "New York"), "201-793-488"));
        PERSON_LIST.add(new GridDemo.Person(102, "Samuel", "Lee", 2, new GridDemo.Address("86829", "New York"), "043-713-538"));
        PERSON_LIST.add(new GridDemo.Person(103, "Lydia", "Ross", 73, new GridDemo.Address("63521", "New York"), "150-813-6462"));
        PERSON_LIST.add(new GridDemo.Person(104, "Aaron", "Atkinson", 17, new GridDemo.Address("25415", "Washington"), "321-679-8544"));
        PERSON_LIST.add(new GridDemo.Person(105, "London", "Woodward", 16, new GridDemo.Address("95632", "New York"), "187-338-588"));
        PERSON_LIST.add(new GridDemo.Person(106, "Dominic", "Riddle", 98, new GridDemo.Address("26009", "Washington"), "583-649-402"));
        PERSON_LIST.add(new GridDemo.Person(107, "Addison", "Nolan", 16, new GridDemo.Address("11798", "New York"), "703-783-806"));
        PERSON_LIST.add(new GridDemo.Person(108, "Bentley", "Hickman", 52, new GridDemo.Address("46788", "New York"), "367-339-0976"));
        PERSON_LIST.add(new GridDemo.Person(109, "Caleb", "Knapp", 22, new GridDemo.Address("14271", "New York"), "922-563-797"));
        PERSON_LIST.add(new GridDemo.Person(110, "Brooklyn", "Giles", 98, new GridDemo.Address("95309", "Washington"), "781-732-052"));
        PERSON_LIST.add(new GridDemo.Person(111, "Brody", "Pittman", 25, new GridDemo.Address("65772", "New York"), "743-631-217"));
        PERSON_LIST.add(new GridDemo.Person(112, "Cameron", "Paul", 95, new GridDemo.Address("55196", "Washington"), "483-665-636"));
        PERSON_LIST.add(new GridDemo.Person(113, "Hailey", "Blackburn", 77, new GridDemo.Address("58674", "Washington"), "207-043-1450"));
        PERSON_LIST.add(new GridDemo.Person(114, "Genesis", "Payne", 47, new GridDemo.Address("16695", "New York"), "619-959-621"));
        PERSON_LIST.add(new GridDemo.Person(115, "Kayden", "Allen", 37, new GridDemo.Address("07295", "New York"), "215-605-108"));
        PERSON_LIST.add(new GridDemo.Person(116, "Lauren", "Calhoun", 46, new GridDemo.Address("55229", "Washington"), "533-854-9338"));
        PERSON_LIST.add(new GridDemo.Person(117, "Julia", "Lawson", 41, new GridDemo.Address("79355", "Washington"), "868-146-7450"));
        PERSON_LIST.add(new GridDemo.Person(118, "Trinity", "Suarez", 67, new GridDemo.Address("94068", "Washington"), "013-185-8057"));
        PERSON_LIST.add(new GridDemo.Person(119, "Jack", "Pace", 97, new GridDemo.Address("30638", "New York"), "987-173-689"));
        PERSON_LIST.add(new GridDemo.Person(120, "Avery", "Reeves", 44, new GridDemo.Address("14575", "Washington"), "460-521-844"));
        PERSON_LIST.add(new GridDemo.Person(121, "Naomi", "Wheeler", 13, new GridDemo.Address("54492", "New York"), "921-646-6501"));
        PERSON_LIST.add(new GridDemo.Person(122, "Anna", "Meyers", 32, new GridDemo.Address("12552", "New York"), "779-107-455"));
        PERSON_LIST.add(new GridDemo.Person(123, "Elizabeth", "Fry", 59, new GridDemo.Address("92279", "Washington"), "320-739-896"));
        PERSON_LIST.add(new GridDemo.Person(124, "Elizabeth", "Chan", 8, new GridDemo.Address("39323", "New York"), "349-528-0627"));
        PERSON_LIST.add(new GridDemo.Person(125, "Nathan", "Mayo", 80, new GridDemo.Address("22058", "Washington"), "764-729-756"));
        PERSON_LIST.add(new GridDemo.Person(126, "Oliver", "Maddox", 33, new GridDemo.Address("56336", "Washington"), "390-477-0228"));
        PERSON_LIST.add(new GridDemo.Person(127, "Joshua", "Pruitt", 16, new GridDemo.Address("73525", "New York"), "878-354-976"));
        PERSON_LIST.add(new GridDemo.Person(128, "Ayden", "Mcpherson", 57, new GridDemo.Address("79196", "Washington"), "764-908-061"));
        PERSON_LIST.add(new GridDemo.Person(129, "Logan", "Bowers", 93, new GridDemo.Address("28479", "New York"), "528-138-7703"));
        PERSON_LIST.add(new GridDemo.Person(130, "Brandon", "Austin", 2, new GridDemo.Address("34148", "Washington"), "523-025-4938"));
        PERSON_LIST.add(new GridDemo.Person(131, "Lydia", "Harmon", 98, new GridDemo.Address("42970", "Washington"), "643-754-1623"));
        PERSON_LIST.add(new GridDemo.Person(132, "Ella", "Casey", 40, new GridDemo.Address("08272", "Washington"), "488-210-4300"));
        PERSON_LIST.add(new GridDemo.Person(133, "Avery", "Ratliff", 96, new GridDemo.Address("34098", "Washington"), "394-174-2895"));
        PERSON_LIST.add(new GridDemo.Person(134, "Ellie", "Barnett", 94, new GridDemo.Address("83127", "New York"), "243-682-1168"));
        PERSON_LIST
                .add(new GridDemo.Person(135, "Katherine", "Hutchinson", 9, new GridDemo.Address("14623", "Washington"), "798-828-891"));
        PERSON_LIST.add(new GridDemo.Person(136, "Charlotte", "Hensley", 17, new GridDemo.Address("33114", "Washington"), "225-075-734"));
        PERSON_LIST.add(new GridDemo.Person(137, "Ella", "Branch", 74, new GridDemo.Address("46839", "New York"), "750-110-959"));
        PERSON_LIST.add(new GridDemo.Person(138, "Nathan", "Mitchell", 13, new GridDemo.Address("86042", "Washington"), "648-871-354"));
        PERSON_LIST.add(new GridDemo.Person(139, "Alyssa", "Parks", 57, new GridDemo.Address("12649", "New York"), "614-666-9736"));
        PERSON_LIST.add(new GridDemo.Person(140, "Alexis", "Stewart", 40, new GridDemo.Address("60533", "New York"), "232-035-7639"));
        PERSON_LIST.add(new GridDemo.Person(141, "Morgan", "Pierce", 6, new GridDemo.Address("69395", "New York"), "794-854-748"));
        PERSON_LIST.add(new GridDemo.Person(142, "Christian", "Preston", 50, new GridDemo.Address("37023", "New York"), "965-398-621"));
        PERSON_LIST.add(new GridDemo.Person(143, "Lucy", "Park", 55, new GridDemo.Address("15242", "Washington"), "248-070-170"));
        PERSON_LIST.add(new GridDemo.Person(144, "Grace", "Hickman", 35, new GridDemo.Address("08264", "Washington"), "935-267-585"));
        PERSON_LIST.add(new GridDemo.Person(145, "Jose", "Ramsey", 72, new GridDemo.Address("86534", "Washington"), "544-463-385"));
        PERSON_LIST.add(new GridDemo.Person(146, "Brayden", "Austin", 66, new GridDemo.Address("00718", "New York"), "750-141-498"));
        PERSON_LIST.add(new GridDemo.Person(147, "Noah", "Boyle", 18, new GridDemo.Address("36042", "Washington"), "499-020-539"));
        PERSON_LIST.add(new GridDemo.Person(148, "Aaron", "Kirk", 5, new GridDemo.Address("71636", "New York"), "153-560-0173"));
        PERSON_LIST.add(new GridDemo.Person(149, "Ellie", "Fox", 24, new GridDemo.Address("76275", "Washington"), "374-833-344"));
        PERSON_LIST.add(new GridDemo.Person(150, "Kevin", "West", 89, new GridDemo.Address("84767", "Washington"), "713-116-1641"));
        PERSON_LIST.add(new GridDemo.Person(151, "Arianna", "Walls", 55, new GridDemo.Address("50743", "New York"), "107-384-6957"));
        PERSON_LIST.add(new GridDemo.Person(152, "Sophie", "Mason", 75, new GridDemo.Address("51583", "New York"), "650-122-387"));
        PERSON_LIST.add(new GridDemo.Person(153, "Victoria", "Fuentes", 30, new GridDemo.Address("27279", "New York"), "162-668-414"));
        PERSON_LIST.add(new GridDemo.Person(154, "Grayson", "Gomez", 9, new GridDemo.Address("37831", "Washington"), "486-710-292"));
        PERSON_LIST.add(new GridDemo.Person(155, "Harper", "Dejesus", 24, new GridDemo.Address("88595", "New York"), "106-607-2643"));
        PERSON_LIST.add(new GridDemo.Person(156, "Leah", "Walter", 27, new GridDemo.Address("65493", "Washington"), "119-077-2876"));
        PERSON_LIST.add(new GridDemo.Person(157, "Evan", "Howell", 21, new GridDemo.Address("14508", "Washington"), "358-362-458"));
        PERSON_LIST.add(new GridDemo.Person(158, "Carson", "Bates", 86, new GridDemo.Address("03913", "New York"), "486-346-2360"));
        PERSON_LIST.add(new GridDemo.Person(159, "Ava", "Villarreal", 55, new GridDemo.Address("89413", "New York"), "904-077-8070"));
        PERSON_LIST.add(new GridDemo.Person(160, "Hailey", "Levy", 100, new GridDemo.Address("23729", "New York"), "405-310-9377"));
        PERSON_LIST.add(new GridDemo.Person(161, "Adam", "Noble", 70, new GridDemo.Address("42068", "Washington"), "354-349-742"));
        PERSON_LIST.add(new GridDemo.Person(162, "Genesis", "Griffin", 20, new GridDemo.Address("53528", "New York"), "081-405-279"));
        PERSON_LIST.add(new GridDemo.Person(163, "Kimberly", "Rogers", 100, new GridDemo.Address("56698", "Washington"), "188-217-9492"));
        PERSON_LIST.add(new GridDemo.Person(164, "Jacob", "Witt", 30, new GridDemo.Address("15892", "Washington"), "059-625-8858"));
        PERSON_LIST.add(new GridDemo.Person(165, "Genesis", "Cervantes", 2, new GridDemo.Address("54556", "New York"), "974-754-449"));
        PERSON_LIST.add(new GridDemo.Person(166, "Hannah", "Tate", 85, new GridDemo.Address("92511", "Washington"), "066-316-3226"));
        PERSON_LIST.add(new GridDemo.Person(167, "Madison", "Lester", 65, new GridDemo.Address("65552", "New York"), "438-002-4236"));
        PERSON_LIST.add(new GridDemo.Person(168, "Anna", "Holder", 54, new GridDemo.Address("08856", "Washington"), "436-526-318"));
        PERSON_LIST.add(new GridDemo.Person(169, "Tyler", "Yates", 3, new GridDemo.Address("06492", "New York"), "129-138-351"));
        PERSON_LIST.add(new GridDemo.Person(170, "Riley", "Joyner", 1, new GridDemo.Address("86459", "New York"), "865-584-663"));
        PERSON_LIST.add(new GridDemo.Person(171, "Layla", "Patterson", 38, new GridDemo.Address("73436", "Washington"), "211-903-653"));
        PERSON_LIST.add(new GridDemo.Person(172, "Carlos", "Conley", 24, new GridDemo.Address("60010", "New York"), "510-415-878"));
        PERSON_LIST.add(new GridDemo.Person(173, "Audrey", "Wilder", 51, new GridDemo.Address("08127", "Washington"), "602-108-8158"));
        PERSON_LIST.add(new GridDemo.Person(174, "Brooklyn", "Lynch", 51, new GridDemo.Address("72703", "Washington"), "013-197-9281"));
        PERSON_LIST.add(new GridDemo.Person(175, "Madison", "Delaney", 80, new GridDemo.Address("95403", "New York"), "091-617-624"));
        PERSON_LIST.add(new GridDemo.Person(176, "Audrey", "Deleon", 76, new GridDemo.Address("36646", "Washington"), "934-776-417"));
        PERSON_LIST.add(new GridDemo.Person(177, "Faith", "Chavez", 84, new GridDemo.Address("66493", "Washington"), "448-266-978"));
        PERSON_LIST.add(new GridDemo.Person(178, "Isaac", "Strickland", 25, new GridDemo.Address("75905", "Washington"), "915-088-178"));
        PERSON_LIST.add(new GridDemo.Person(179, "David", "Hawkins", 76, new GridDemo.Address("64090", "New York"), "356-414-0537"));
        PERSON_LIST.add(new GridDemo.Person(180, "Kennedy", "Ross", 6, new GridDemo.Address("61083", "New York"), "768-806-0809"));
        PERSON_LIST.add(new GridDemo.Person(181, "Morgan", "Hicks", 9, new GridDemo.Address("47757", "Washington"), "083-755-6514"));
        PERSON_LIST.add(new GridDemo.Person(182, "Samuel", "Brewer", 2, new GridDemo.Address("17009", "New York"), "815-599-5344"));
        PERSON_LIST.add(new GridDemo.Person(183, "Cooper", "House", 9, new GridDemo.Address("99203", "New York"), "688-672-886"));
        PERSON_LIST.add(new GridDemo.Person(184, "Kevin", "Kane", 25, new GridDemo.Address("15223", "Washington"), "479-538-704"));
        PERSON_LIST.add(new GridDemo.Person(185, "Mason", "Witt", 72, new GridDemo.Address("75790", "Washington"), "923-649-1904"));
        PERSON_LIST.add(new GridDemo.Person(186, "Morgan", "Brady", 56, new GridDemo.Address("28453", "Washington"), "702-984-0476"));
        PERSON_LIST.add(new GridDemo.Person(187, "Jonathan", "King", 27, new GridDemo.Address("31475", "New York"), "596-937-1324"));
        PERSON_LIST.add(new GridDemo.Person(188, "Julia", "Mcintosh", 98, new GridDemo.Address("35470", "New York"), "352-037-610"));
        PERSON_LIST.add(new GridDemo.Person(189, "Levi", "Mcintyre", 24, new GridDemo.Address("53683", "New York"), "665-171-7781"));
        PERSON_LIST.add(new GridDemo.Person(190, "Violet", "Benson", 95, new GridDemo.Address("15838", "Washington"), "008-256-4939"));
        PERSON_LIST.add(new GridDemo.Person(191, "Aaron", "Oneal", 34, new GridDemo.Address("58133", "New York"), "581-435-334"));
        PERSON_LIST.add(new GridDemo.Person(192, "David", "Carver", 57, new GridDemo.Address("02488", "New York"), "751-648-5664"));
        PERSON_LIST.add(new GridDemo.Person(193, "Brody", "Stephens", 38, new GridDemo.Address("70033", "Washington"), "198-808-5304"));
        PERSON_LIST.add(new GridDemo.Person(194, "Isaac", "Ford", 59, new GridDemo.Address("63121", "New York"), "704-797-550"));
        PERSON_LIST.add(new GridDemo.Person(195, "Ella", "Charles", 94, new GridDemo.Address("90960", "Washington"), "134-165-959"));
        PERSON_LIST.add(new GridDemo.Person(196, "Emma", "Salas", 78, new GridDemo.Address("07302", "Washington"), "928-075-889"));
        PERSON_LIST.add(new GridDemo.Person(197, "Ryan", "Kramer", 60, new GridDemo.Address("05719", "Washington"), "342-504-013"));
        PERSON_LIST.add(new GridDemo.Person(198, "Charlotte", "Bass", 36, new GridDemo.Address("44576", "New York"), "653-360-8595"));
        PERSON_LIST.add(new GridDemo.Person(199, "Easton", "Vang", 25, new GridDemo.Address("81988", "Washington"), "866-787-6577"));
    }

    public List<GridDemo.Person> getPersons() {
        return PERSON_LIST;
    }
}
