import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.Scanner;

public class TabuSearch {

    static final double OPTYMALNA = 2020;
    public static int rozmiar = 30;
    public static double[][] odleglosci;
    static String wyniki = "";

    public static void main(String[] args) throws IOException {

        int proba = 1;
        double roznica = 0;
        double procent = 0;
        double[][] tablica = new double[rozmiar][2];
        int iteracje = 10;
        TabuList tabuList = new TabuList(rozmiar);

        odczytDanychZPliku(tablica);

        //obliczenie odległości między miastami
        odleglosci = obliczOdlegosci(tablica);

        //wyswietlKolejnoscMiast(randomizeArray(1, 129));

        //iteracje testów
        for (int j = 0; j < 6; j++) {
            //rozpoczęcie mierzenia czasu działania
            Instant startTime = java.time.Instant.now();
            iteracje = iteracje * 10;
            //wypełnienie tablicy z rozwiązaniem początkowym w tym wypadku rozwiązanie początkowe
            // to kolejność wczytania kolejnych miast
            int[] tempSolution = randomizeArray(1, rozmiar - 2);
            wyswietlKolejnoscMiast(tempSolution);
            for (int i = 0; i < tempSolution.length; i++) {
                tempSolution[i] = i;
                if (tempSolution[i] == tempSolution.length - 1) {
                    tempSolution[i] = 0;
                }
            }

            int[] bestSolution = new int[tempSolution.length];
            System.arraycopy(tempSolution, 0, bestSolution, 0, bestSolution.length);
            int koszt = getTrasa(bestSolution);

            //początek przeszukiwania
            for (int i = 0; i < iteracje; i++) {
                tempSolution = TabuSearch.przeszukajSasiedztwo(tabuList, tempSolution);
                int tempKoszt = getTrasa(tempSolution);
                if (tempKoszt < koszt) {
                    System.arraycopy(tempSolution, 0, bestSolution, 0, bestSolution.length);
                    koszt = tempKoszt;
                }
            }

            //zakończenie mierzenia czasu działania
            Instant endTime = java.time.Instant.now();

            //wyświetlenie wyników
            System.out.println("Łączna długość przebytej trasy = " + koszt);
            System.out.println("Proponowana kolejność odwiedzania miast:");
            wyswietlKolejnoscMiast(bestSolution);

            roznica = koszt - OPTYMALNA;
            procent = (1 - (OPTYMALNA / koszt)) * 100;

            //obliczanie czasu trwania
            Duration czasTrwania = java.time.Duration.between(startTime, endTime);
            System.out.println("czas wykonania: " + czasTrwania.getSeconds() + " sekund");
            wyniki += proba + " " + czasTrwania.getSeconds() + " " + koszt + " " + roznica + " " + procent + "\n";
            proba++;
        }
        zapiszDaneDoPliku(wyniki);
    }

    //wyswietlanie kolejnosci miast
    public static void wyswietlKolejnoscMiast(int[] solution) {
        for (int i = 0; i < solution.length; i++) {
            System.out.print(solution[i] + " ");
        }
        System.out.println();
    }

    //czytanie danych z pliku zewnętrznego
    static double[][] odczytDanychZPliku(double tablica[][]) throws IOException {
        FileReader fr = new FileReader("bays29.tsp");
        //FileReader fr = new FileReader("doTestow.tsp");
        //FileReader fr = new FileReader("ch130.tsp");
        Scanner sc = new Scanner(fr);
        int j = 0;
        while (sc.hasNext()) {
            String linia = sc.nextLine();
            String[] temp = linia.split(" ");
            tablica[j][0] = Double.parseDouble(temp[1]);
            tablica[j][1] = Double.parseDouble(temp[2]);
            j++;
        }
        fr.close();
        return tablica;
    }

    //wypelnienie tablicy z odleglosciami miedzy miastami
    private static double[][] obliczOdlegosci(double[][] tablica) {

        double[][] odleglosci = new double[tablica.length][tablica.length];
        for (int i = 0; i < tablica.length; i++) {
            for (int j = 0; j < tablica.length; j++) {
                odleglosci[i][j] = Math.sqrt(Math.pow(tablica[j][0] - tablica[i][0], 2) + Math.pow(tablica[j][1] - tablica[i][1], 2));
                //System.out.print(odleglosci[i][j] + " \t \t \t");
            }
            //System.out.println();
        }
        return odleglosci;
    }

    //przeszukiwanie sąsiedztwa
    public static int[] przeszukajSasiedztwo(TabuList tabuList, int[] initSolution) {

        int[] bestSol = new int[initSolution.length];

        //kopiowanie tablicy z rozwiązaniem początkowym do tablicy najlepszego rozwiązania
        System.arraycopy(initSolution, 0, bestSol, 0, bestSol.length);
        int bestCost = getTrasa(initSolution);
        int miasto1 = 0;
        int miasto2 = 0;
        boolean firstNeighbor = true;

        for (int i = 1; i < bestSol.length - 1; i++) {
            for (int j = 2; j < bestSol.length - 1; j++) {
                if (i == j) {
                    continue;
                }

                int[] newBestSol = new int[bestSol.length];
                System.arraycopy(bestSol, 0, newBestSol, 0, newBestSol.length);

                //zamiana wybranych miast aby sprawdzić czy może to będzie lepsze rozwiązanie
                newBestSol = zamienMiasta(i, j, initSolution);
                int newBestCost = getTrasa(newBestSol);

                //zapamiętanie lepszego rozwiązania jeśli takie zostanie znalezione
                if ((newBestCost < bestCost || firstNeighbor) && tabuList.tabuList[i][j] == 0) {
                    firstNeighbor = false;
                    miasto1 = i;
                    miasto2 = j;
                    System.arraycopy(newBestSol, 0, bestSol, 0, newBestSol.length);
                    bestCost = newBestCost;
                }
            }
        }

        if (miasto1 != 0) {
            tabuList.zmniejszKare();
            tabuList.narzucenieKary(miasto1, miasto2);
        }
        return bestSol;
    }

    //zamiana miast
    public static int[] zamienMiasta(int m1, int m2, int[] solution) {
        int temp = solution[m1];
        solution[m1] = solution[m2];
        solution[m2] = temp;

        return solution;
    }

    //obliczanie długości całej trasy
    public static int getTrasa(int solution[]) {

        int trasa = 0;
        for (int i = 0; i < solution.length - 1; i++) {
            trasa += odleglosci[solution[i]][solution[i + 1]];
        }

        return trasa;
    }

    static public void zapiszDaneDoPliku(String wyniki) throws IOException {
        //FileWriter fw = new FileWriter("tsp_wyniki_130.txt");
        //FileWriter fw = new FileWriter("tsp_wyniki_1002.txt");
        FileWriter fw = new FileWriter("bays29_wyniki.txt");
        fw.write(wyniki);
        fw.close(); //zamknięcie pliku
    }

    public static int[] randomizeArray(int a, int b) {
        Random rgen = new Random();  // Random number generator
        int size = b + 2;
        int[] array = new int[size];

        for (int i = 1; i < b+1; i++) {
            array[i] = i;
        }
        System.out.println();
        for (int i = 1; i < array.length - 1; i++) {
            int randomPosition = rgen.nextInt(array.length-2);
            int temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
        array[0] = 0;
        array[array.length - 1] = 0;
        return array;
    }
}