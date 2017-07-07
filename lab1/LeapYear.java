
public class LeapYear {
	public boolean isLeapYear(int year){
		if(year%400==0||(year%4==0&&year%100!=0)){
			return true;
		}
		else return false;
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LeapYear y = new LeapYear();
		int year=2000;
		if(y.isLeapYear(year)){
			System.out.println(year+" is a leap year.");
		}
	}

}
