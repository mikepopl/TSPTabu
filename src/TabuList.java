public class TabuList {

    int[][] tabuList;

    public TabuList(int numCities) {
        tabuList = new int[numCities][numCities];
    }


    public void narzucenieKary(int city1, int city2) {

        tabuList[city1][city2] += 10;
        tabuList[city2][city1] += 10;
    }


    public void zmniejszKare() {
        for (int i = 0; i < tabuList.length; i++) {
            for (int j = 0; j < tabuList.length; j++) {
                tabuList[i][j] -= tabuList[i][j] <= 0 ? 0 : 1;
            }
        }
    }


    public void wyswietlTabuList(){
        System.out.println("początek tablicy tabu");
        for (int i=0;i<tabuList.length;i++){
            for (int j=0;j<tabuList.length;j++){
                System.out.print(tabuList[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }
}
