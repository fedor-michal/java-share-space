package com.fedordevelopment.regexy;

import com.fedordevelopment.common.Validate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Person {

    private String name;
    private String surname;
    //private Pesel pesel;
    private String pesel;
    private String sex;

    private static List<Person> personExtent = new ArrayList<>();

    private static Set<String> uniquePesels = new HashSet<>();

    public Person(String name, String surname, String pesel, String sex) {
        validateName(name);
        validateSurname(surname);
        validateSex(sex);
        checkPesel(pesel);
        checkPeselUniqueness(pesel);
        checkSexWithPesel(pesel, sex);
        this.name = name;
        this.surname = surname;
        this.pesel = pesel;
        this.sex = sex;
        personExtent.add(this);
        uniquePesels.add(pesel);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public String getSurname() {

        return surname;
    }

    public void setSurname(String surname) {
        validateSurname(surname);
        this.surname = surname;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        checkPesel(pesel);
        checkPeselUniqueness(pesel);
        uniquePesels.remove(this.pesel);
        uniquePesels.add(pesel);
        this.pesel = pesel;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        validateSex(sex);
        this.sex = sex;
    }

    public List<Person> getPersonExtent() {
        return new ArrayList<>(personExtent);
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", pesel='" + pesel + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }

    public LocalDate getPersonDateOfBirth() {
        Pattern pattern = Pattern.compile("(?<year>\\d{2})(?<month>\\d{2})(?<day>\\d{2})(?:\\d{5})");
        Matcher matcher = pattern.matcher(pesel);
        matcher.matches();
        int year = getYearOfBirth(matcher.group("year"), matcher.group("month"));
        int month = getMonthOfBirth(matcher.group("month"));
        int day = getDayOfBirth(matcher.group("day"));
        return LocalDate.of(year, month, day);
    }

    private static int getYearOfBirth(String year, String month) {
        int searchedInt;
        //char monthFirstDigit = month.charAt(0);
        int monthFirstDigit = Character.getNumericValue(month.charAt(0));
        if (monthFirstDigit == 0 || monthFirstDigit == 1) {
            searchedInt = 1900 + Integer.valueOf(year);
        } else if (monthFirstDigit == 2 || monthFirstDigit == 3) {
            searchedInt = 2000 + Integer.valueOf(year);
        } else {
            throw new IllegalArgumentException("Pesel number out of birth date range (1900-2099)");
        }
        return searchedInt;
    }

    private static int getMonthOfBirth(String month) {
        // przypadki dla lat 1900-2099:
        int searchedInt;
        int firstDigit = Character.getNumericValue(month.charAt(0));
        if (firstDigit == 0 || firstDigit == 2) {
            searchedInt = Character.getNumericValue(month.charAt(1));
        } else if (firstDigit == 1) {
            searchedInt = Integer.valueOf(month);
        } else if (firstDigit == 3) {
            searchedInt = Integer.valueOf(month) - 20;
        } else {
            throw new IllegalArgumentException("Pesel number out of birth date range (1900-2099)");
        }
        return searchedInt;
    }

    private static int getDayOfBirth(String day) {
        int searchedInt;
        if (day.charAt(0) == 0) {
            searchedInt = Character.getNumericValue(day.charAt(1));
        } else {
            searchedInt = Integer.valueOf(day);
        }
        return searchedInt;
    }

    private static void validateName(String name) {
        Validate.nonNull(name, "name");
        if (!name.matches("\\p{Upper}\\p{Lower}+")) {
            throw new IllegalArgumentException("Given name is invalid.");
        }
    }

    private static void validateSurname(String surname) {
        Validate.nonNull(surname, "Surname");
        if (!surname.matches("\\p{Upper}\\p{Lower}+(-\\p{Upper}\\p{Lower}+)?")) {
            throw new IllegalArgumentException("Given surname is invalid.");
        }
    }

    private static void validateSex(String sex) {
        Validate.nonNull(sex, "Sex");
        // mógłby być też regex: (fe)?male
        if (!(sex.equalsIgnoreCase("male") || sex.equalsIgnoreCase("female"))) {
            throw new IllegalArgumentException("Given name should be from: male/female .");
        }
    }

    private static void checkPeselUniqueness(String pesel) {
        if (uniquePesels.contains(pesel)) {
            throw new IllegalArgumentException(String.format("This passenger with given pesel: {%s} already exists in system", pesel));
        }
    }

    private static void checkPesel(String pesel) {
        Validate.nonNull(pesel, "Pesel");
        // ( od razu sprawedzam czy każdy znak jest liczbą )
        if (!pesel.matches("\\d{11}")) {
            throw new IllegalArgumentException("Passenger's PESEL number needs to be 11 digits long.");
        }
        if (!isPeselControlDigitCorrect(pesel)) {
            throw new IllegalArgumentException("PESEL number is invalid.");
        }
    }

    private static boolean isPeselControlDigitCorrect(String pesel) {
/*         Algorytm sprawdzania cyfry kontrolnej numeru pesel:
         1. Pomnóż każdą cyfrę z numeru PESEL przez odpowiednią wagę: 1-3-7-9-1-3-7-9-1-3. (10 pierwszych cyfr bo 11. jest kontrolna)
         2. Dodaj do siebie cyfry jedności otrzymanych wyników (na przykład zamiast 63 dodaj 3).
         3. Odejmij cyfrę jedności uzyskanego wyniku od 10. Cyfra, która uzyskasz, to cyfra kontrolna.*/
        List<Integer> controlDigitCalculationConstant = Arrays.asList(1, 3, 7, 9, 1, 3, 7, 9, 1, 3);
        List<Integer> peselDigits = getPeselAsDigits(pesel);
        int tmpSum = 0;
        for (int i = 0; i < peselDigits.size() - 1; i++) {
            tmpSum += (peselDigits.get(i) * controlDigitCalculationConstant.get(i)) % 10;
        }
        int peselControlNumber = 10 - (tmpSum % 10);
        return peselDigits.get(peselDigits.size() - 1) == peselControlNumber;
    }

    private static List<Integer> getPeselAsDigits(String pesel) {
        List<Integer> peselDigits = new LinkedList<>();
//        for (Character ch: pesel.toCharArray()) {
//            peselDigits.add(Character.getNumericValue(ch));
//        }
        for (int i = 0; i < pesel.length(); i++) {
            char tmpDigitAsChar = pesel.charAt(i);
            int tmpDigit = Character.getNumericValue(tmpDigitAsChar);
            peselDigits.add(tmpDigit);
        }
        return peselDigits;
    }

    private static void checkSexWithPesel(String pesel, String sex) {

        // PPPP - to liczba porządkowa oznaczająca płeć.
        // U kobiety ostatnia cyfra tej liczby jest parzysta.

        //sposob 1: długi ale do nauki regex:
        Pattern pattern = Pattern.compile("\\d{9}(?<sexDigit>\\d)\\d");
        Matcher matcher = pattern.matcher(pesel);
        matcher.matches();
        int sexDigit = Integer.parseInt(matcher.group("sexDigit"));
        //sposób 2 - chyba dużo lepszy
        //int sexDigit = Character.getNumericValue(pesel.charAt(pesel.length()-2) );
        if (sexDigit % 2 == 0) {
            if (!sex.equalsIgnoreCase("female")) {
                throw new IllegalArgumentException("Error: this pesel number belongs to female person");
            }
        }
    }
}
