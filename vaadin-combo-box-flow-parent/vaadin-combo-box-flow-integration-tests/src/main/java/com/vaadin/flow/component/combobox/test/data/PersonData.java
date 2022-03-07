package com.vaadin.flow.component.combobox.test.data;

import com.vaadin.flow.component.combobox.test.entity.Person;

import java.util.ArrayList;
import java.util.List;

public class PersonData {

    private static final String[] names = new String[] { "Aaron", "Adam",
            "Addison", "Alexis", "Alyssa", "Andrew", "Anna", "Anton", "Arianna",
            "Audrey", "Ava", "Avery", "Ayden", "Bentley", "Brandon", "Brayden",
            "Brody", "Brooklyn", "Caleb", "Cameron", "Carlos", "Carson",
            "Charlotte", "Christian", "Cooper", "David", "Dominic", "Easton",
            "Elizabeth", "Ella", "Ellie", "Emma", "Evan", "Faith", "Genesis",
            "Grace", "Grayson", "Hailey", "Hannah", "Harper", "Isaac", "Jack",
            "Jacob", "Jonathan", "Jose", "Joshua", "Julia", "Katherine",
            "Kayden", "Kennedy", "Kevin", "Kimberly", "Lauren", "Layla", "Leah",
            "Levi", "Logan", "London", "Lucy", "Lydia", "Madison", "Mason",
            "Mia", "Mickael", "Morgan", "Naomi", "Nathan", "Noah", "Oliver",
            "Peter", "Riley", "Ryan", "Samuel", "Sophie", "Trinity", "Tyler",
            "Victoria", "Violet" };
    private static final String[] surnames = new String[] { "Allen", "Atkinson",
            "Austin", "Barnett", "Bass", "Bates", "Bauer", "Benson",
            "Blackburn", "Blackwell", "Bowers", "Boyle", "Brady", "Branch",
            "Brewer", "Buchanan", "Calhoun", "Carver", "Casey", "Cervantes",
            "Chan", "Charles", "Chavez", "Conley", "Dejesus", "Delaney",
            "Deleon", "Ford", "Fox", "Fry", "Fuentes", "Giles", "Gomez",
            "Griffin", "Harmon", "Hawkins", "Hensley", "Hickman", "Hicks",
            "Holder", "House", "Howell", "Hutchinson", "Joyner", "Kane", "King",
            "Kirk", "Knapp", "Kramer", "Lawson", "Lee", "Lester", "Levy",
            "Lynch", "Maddox", "Mason", "Mayo", "Mcintosh", "Mcintyre",
            "Mcpherson", "Meyers", "Mitchell", "Noble", "Nolan", "Oneal",
            "Pace", "Park", "Parks", "Patterson", "Paul", "Payne", "Pierce",
            "Pittman", "Preston", "Pruitt", "Ramsey", "Ratliff", "Reeves",
            "Riddle", "Rogers", "Ross", "Salas", "Stephens", "Stewart",
            "Strickland", "Suarez", "Tate", "Vang", "Villarreal", "Walls",
            "Walter", "West", "Wheeler", "Wilder", "Witt", "Woodward",
            "Yates" };
    private static final int[] ages = new int[] { 100, 11, 13, 16, 17, 18, 20,
            21, 22, 24, 25, 27, 28, 30, 32, 33, 34, 35, 36, 37, 38, 40, 41, 44,
            46, 47, 50, 51, 52, 53, 54, 55, 56, 57, 59, 60, 65, 66, 67, 68, 70,
            72, 73, 74, 75, 76, 77, 78, 80, 84, 85, 86, 89, 93, 94, 95, 96, 97,
            98 };
    private static final String[] numbers = new String[] { "00718", "02488",
            "03913", "05719", "06492", "07295", "07302", "08127", "08264",
            "08272", "08856", "11798", "12080", "12552", "12649", "14271",
            "14508", "14575", "14623", "15223", "15242", "15838", "15892",
            "16695", "17009", "22058", "23729", "25415", "26009", "27279",
            "28453", "28479", "30638", "31475", "33114", "34098", "34148",
            "35470", "36042", "36646", "37023", "37831", "39323", "42068",
            "42970", "44576", "46788", "46839", "47757", "50743", "51583",
            "53528", "53683", "54492", "54556", "55196", "55229", "56336",
            "56698", "58133", "58674", "60010", "60533", "61083", "63121",
            "63521", "64090", "65493", "65552", "65772", "66493", "69395",
            "70033", "71636", "72703", "73436", "73525", "75790", "75905",
            "76275", "79196", "79355", "81988", "83127", "84767", "86042",
            "86459", "86534", "86829", "88595", "89413", "90960", "92279",
            "92511", "93849", "94068", "95309", "95403", "95632", "99203" };
    private static final String[] phones = new String[] { "008-256-4939",
            "013-185-8057", "013-197-9281", "043-713-538", "059-625-8858",
            "066-316-3226", "081-405-279", "083-755-6514", "091-617-624",
            "106-607-2643", "107-384-6957", "107-922-137", "119-077-2876",
            "120-952-285", "127-942-237", "127-949-238", "129-138-351",
            "134-165-959", "150-813-6462", "153-560-0173", "162-668-414",
            "187-338-588", "188-217-9492", "198-808-5304", "201-793-488",
            "207-043-1450", "211-903-653", "215-605-108", "225-075-734",
            "232-035-7639", "243-682-1168", "248-070-170", "320-739-896",
            "321-679-8544", "342-504-013", "349-528-0627", "352-037-610",
            "354-349-742", "356-414-0537", "358-362-458", "367-339-0976",
            "374-833-344", "390-477-0228", "394-174-2895", "405-310-9377",
            "436-526-318", "438-002-4236", "448-266-978", "460-521-844",
            "479-538-704", "483-665-636", "486-346-2360", "486-710-292",
            "488-210-4300", "499-020-539", "510-415-878", "523-025-4938",
            "528-138-7703", "533-854-9338", "544-463-385", "581-435-334",
            "583-649-402", "596-937-1324", "602-108-8158", "614-666-9736",
            "619-959-621", "643-754-1623", "648-871-354", "650-122-387",
            "653-360-8595", "665-171-7781", "688-672-886", "702-984-0476",
            "703-783-806", "704-797-550", "713-116-1641", "743-631-217",
            "750-110-959", "750-141-498", "751-648-5664", "764-729-756",
            "764-908-061", "768-806-0809", "779-107-455", "781-732-052",
            "794-854-748", "798-828-891", "815-599-5344", "865-584-663",
            "866-787-6577", "868-146-7450", "878-354-976", "904-077-8070",
            "915-088-178", "921-646-6501", "922-563-797", "923-649-1904",
            "928-075-889", "934-776-417", "935-267-585", "965-398-621",
            "974-754-449", "987-173-689" };
    private static final String[] cities = new String[] { "New York",
            "Washington" };

    private final List<Person> people = new ArrayList<>();

    private final int personsCount;

    public PersonData() {
        this(100);
    }

    public PersonData(int personsCount) {
        this.personsCount = personsCount;
    }

    public List<Person> getPersons() {
        if (people.isEmpty()) {
            for (int i = 0; i <= personsCount; i++) {
                people.add(new Person(i, names[i % names.length],
                        surnames[i % surnames.length], ages[i % ages.length],
                        new Person.Address(numbers[i % numbers.length],
                                cities[i % cities.length]),
                        phones[i % phones.length]));
            }
        }
        return people;
    }
}
