// testando new array (OK)
class m330
{
   public static void main(String[] args)
   {
      System.out.println(new a().i());
   }
}

class a
{
   int[] i;
   public int i(){i = new int[10]; i[2] = 1; return i[2];}
}
