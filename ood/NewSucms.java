
import java.util.*;

public class Main {
    static class UniversityConfig {
        static final UniversityConfig obj=new UniversityConfig();
        int limit=6;
        private UniversityConfig(){}
        static UniversityConfig get(){return obj;}
    }

    interface UserAccount { String id(); }

    static class Student implements UserAccount {
        String i;
        Student(String i){this.i=i;}
        public String id(){return i;}
    }

    static class Faculty implements UserAccount {
        String i;
        Faculty(String i){this.i=i;}
        public String id(){return i;}
    }

    static class Admin implements UserAccount {
        String i;
        Admin(String i){this.i=i;}
        public String id(){return i;}
    }

    static class AccountFactory {
        static UserAccount create(String r,String id){
            if(id==null||id.isBlank())throw new RuntimeException();
            switch(r){
                case "STUDENT":return new Student(id);
                case "FACULTY":return new Faculty(id);
                case "ADMIN":return new Admin(id);
                default:throw new RuntimeException();
            }
        }
    }

    static class Course {
        String code;
        int credits,capacity;
        Set<String> students=new HashSet<>();
        Course(String c,int cr,int cap){
            code=c;credits=cr;capacity=cap;
        }
    }

    static class Registration {
        Map<String,Integer> total=new HashMap<>();
        void register(Student s,Course c){
            int n=total.getOrDefault(s.id,0);
            if(c.students.contains(s.id)||c.students.size()>=c.capacity||
               n+c.credits>UniversityConfig.get().limit)
                throw new RuntimeException();
            c.students.add(s.id);
            total.put(s.id,n+c.credits);
        }
    }

    interface Observer { void update(String r,String m); }

    static class StudentPortal implements Observer {
        String id;
        StudentPortal(String i){id=i;}
        public void update(String r,String m){
            if(r==null||r.equals(id))System.out.println(m);
        }
    }

    static class FacultyPortal implements Observer {
        public void update(String r,String m){
            if(r==null)System.out.println(m);
        }
    }

    static class NoticePublisher {
        List<Observer> list=new ArrayList<>();
        void add(Observer o){list.add(o);}
        void remove(Observer o){list.remove(o);}
        void send(String r,String m){
            for(Observer o:list)o.update(r,m);
        }
    }

    static class Marks {
        double e,c,p;
        Marks(double e,double c,double p){
            for(double x:new double[]{e,c,p})
                if(!Double.isFinite(x)||x<0||x>100)
                    throw new RuntimeException();
            this.e=e;this.c=c;this.p=p;
        }
    }

    interface Strategy { double calculate(Marks m); }

    static class Theory implements Strategy {
        public double calculate(Marks m){return .6*m.e+.4*m.c;}
    }

    static class Lab implements Strategy {
        public double calculate(Marks m){
            return .3*m.e+.2*m.c+.5*m.p;
        }
    }

    static class EvaluationService {
        Strategy s;
        NoticePublisher p;
        EvaluationService(Strategy s,NoticePublisher p){
            this.s=s;this.p=p;
        }
        void set(Strategy x){s=x;}
        double result(Student st,Marks m){
            double score=s.calculate(m);
            p.send(st.id(),"Score: "+score);
            return score;
        }
    }

    static class ExternalLibrary {
        String copies(String id){
            if(id.equals("BOOK1"))return "2";
            if(id.equals("BOOK2"))return "0";
            return "UNKNOWN";
        }
    }

    interface LibraryService { boolean available(String id); }

    static class LibraryAdapter implements LibraryService {
        ExternalLibrary l=new ExternalLibrary();
        public boolean available(String id){
            String n=l.copies(id);
            if(n.equals("UNKNOWN"))throw new RuntimeException();
            return Integer.parseInt(n)>0;
        }
    }

    public static void main(String[] args){
        System.out.println(UniversityConfig.get()==UniversityConfig.get());

        Student s=(Student)AccountFactory.create("STUDENT","S1");
        AccountFactory.create("FACULTY","F1");
        AccountFactory.create("ADMIN","A1");

        Registration r=new Registration();
        Course c1=new Course("C1",3,5);
        Course c2=new Course("C2",3,5);
        r.register(s,c1);
        r.register(s,c2);
        System.out.println(r.total.get(s.id));

        NoticePublisher p=new NoticePublisher();
        StudentPortal p1=new StudentPortal("S1");
        StudentPortal p2=new StudentPortal("S2");
        FacultyPortal f=new FacultyPortal();
        p.add(p1);p.add(p2);p.add(f);
        p.send(null,"Campus Notice");

        Marks m=new Marks(80,70,90);
        EvaluationService e=new EvaluationService(new Theory(),p);
        System.out.println(e.result(s,m));

        e.set(new Lab());
        System.out.println(e.result(s,m));

        p.remove(p2);
        p.send(null,"New Notice");

        LibraryService l=new LibraryAdapter();
        System.out.println(l.available("BOOK1"));
        System.out.println(l.available("BOOK2"));
    }
               }
  
