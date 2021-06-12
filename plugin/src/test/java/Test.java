public class Test {

  public static void main(String[] args) {
    float step = 180;
    for (int x = 0; x < 3; x++) {
      for (int z = 0; z < 3; z++) {
        if (z == 1 || z == 3 || x == 1 || x == 3) {
          System.out.println(x * Math.sin(step));
          System.out.println(z * Math.cos(step));
        }
      }
    }
  }

}
