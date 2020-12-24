package model;


import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//import static model.City.isFieldOfMatrixFree;

public class Resident extends TimerTask {
    private static final int boundaryYearOfBirthAdult=1955;
    private static final int boundaryYearOfBirthChild=2003;
    private static final int boundaryYearOfBirthElder=1900;
    private Long id;
    private String name;
    private String surname;
    private static Integer yearOfBirth;
    private Gender gender;
    private Long houseID;
    private static Double bodyTemperature;
    Random temperature=new Random();
    private final double minTemperature=34;
    private final double maxTemperature=40;
    private static final String arrayOfNames[]={"S","M","K","V","T","U","I","L","D","B"};
    private static final String arrayOfSurnames[]={"1","2","3","4","5","6","7","8","9"};
    public Resident(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.yearOfBirth = yearOfBirth;
        this.gender = gender;
        this.houseID = houseID;
        run();
    }

    public Resident() {
    }
    public void run(){
        Timer timer = new Timer();
        timer.schedule(new Resident(), 0, 30000);
        DecimalFormat df = new DecimalFormat("0.0");
        bodyTemperature=Double.parseDouble(df.format(minTemperature+(temperature.nextDouble()*(maxTemperature-minTemperature))));
    }
    // numberOfResidentsOfConcretType-ukupan broj stanovnika odredjenog tipa(odrasli,djeca ili stari)
    public static void setAdults(Long id,Long houseID) throws NotAdultException {
        Random randomResident=new Random();
        Resident resident;
        int typeOfResident;
        Timer timer = new Timer();
        timer.schedule(new Resident(), 0, 30000);
        Random matrixPosition=new Random();
                        resident=new Adult(id,getNameRandomly(),getSurnameRandomly(),boundaryYearOfBirthAdult+new Random().nextInt(47),choseGenderRandomly(),houseID);
                        System.out.println("god="+yearOfBirth+"temp="+bodyTemperature);
                  }
    public static void setChildren(Long id,Long houseID) throws  NotChildException {
        Random randomResident = new Random();
        Resident resident;
        int typeOfResident;
        Timer timer = new Timer();
        timer.schedule(new Resident(), 0, 30000);
        Random matrixPosition = new Random();
            resident = new Child((long)id, getNameRandomly(), getSurnameRandomly(), boundaryYearOfBirthChild + new Random().nextInt(18), choseGenderRandomly(), houseID);
            System.out.println("god=" + yearOfBirth + "temp=" + bodyTemperature);

        }
        public static void setElders(Long id,Long houseID) throws NotElderException {
            Random randomResident=new Random();
            Resident resident;
            int typeOfResident;
            Timer timer = new Timer();
            timer.schedule(new Resident(), 0, 30000);
            Random matrixPosition=new Random();
                resident=new Elder((long)id,getNameRandomly(),getSurnameRandomly(),Resident.boundaryYearOfBirthElder+new Random().nextInt(55),choseGenderRandomly(),houseID);
                System.out.println("god="+yearOfBirth+"temp="+bodyTemperature);
          }

        public static String getNameRandomly(){
        return arrayOfNames[new Random().nextInt(arrayOfNames.length-1)];
        }
    public static String getSurnameRandomly(){
        return arrayOfSurnames[new Random().nextInt(arrayOfSurnames.length-1)];
    }
    public static Gender choseGenderRandomly(){
        int gender=new Random().nextInt(1);
        if(gender==0){
            return Gender.Female;
        }
        else
            return Gender.Male;
    }
}
//}
