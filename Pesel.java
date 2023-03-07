package com.fedordevelopment.projectx;

import com.fedordevelopment.common.Validate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Pesel {

    private final String pesel;

    private static final Set<String> uniquePesels = new HashSet<>();

    public Pesel(String pesel) {
        checkPesel(pesel);
        checkPeselUniqueness(pesel);
        this.pesel = pesel;
        uniquePesels.add(pesel);
    }

    public String getPesel() {
        return pesel;
    }

    public static Set<String> getUniquePesels() {
        return new HashSet<>(uniquePesels);
    }

    public static void removePesel(Pesel pesel) {
        uniquePesels.remove(pesel);
    }

    public static void checkPesel(String pesel) {
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

    private static void checkPeselUniqueness(String pesel) {
        if (uniquePesels.contains(pesel)) {
            throw new IllegalArgumentException(String.format("This pesel: {%s} already exists in system", pesel));
        }
    }

    @Override
    public String toString() {
        return pesel;
    }
}
