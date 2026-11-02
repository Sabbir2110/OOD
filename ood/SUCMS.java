
import java.util.*;

public class Main {
    static class UniversityConfig {
        static UniversityConfig obj=new UniversityConfig();
        int limit=6;
        private UniversityConfig() {}
    }

    interface UserAccount { String getId(); }

    static class Student implements UserAccount {
        String id;
        Student(String i) { id=i; }
        public String getId() { return id; }
    }

    static class Faculty implements UserAccount {
        String id;
        Faculty(String i) { id=i; }
        public String getId() { return id; }
    }

    static class Admin implements UserAccount {
        String id;
        Admin(String i) { id=i; }
        public String getId() { return id; }
    }

    static class AccountFactory {
        static UserAccount create(String r,String id) {
            if(id==null || id.isBlank()) throw new IllegalArgumentException();
            switch(r) {
                case "STUDENT": return new Student(id);
                case "FACULTY": return new Faculty(id);
                case "ADMIN": return new Admin(id);
                default: throw new IllegalArgumentException();
            }
        }
    }

    static class Course {
        String code;
        int credits,capacity;
        Set<String> students=new HashSet<>();
        Course(String c,int cr,int cap) {
            code=c; credits=cr; capacity=cap;
        }
    }

    static class Registration {
        Map<String,Integer> total=new HashMap<>();
        void register(Student s,Course c) {
            int n=total.getOrDefault(s.id,0);
            if(c.students.contains(s.id) ||
               c.students.size()>=c.capacity ||
               n+c.credits>UniversityConfig.obj.limit)
                throw new IllegalArgumentException();
            c.students.add(s.id);
            total.put(s.id,n+c.credits);
        }
    }

    interface Observer { void update(String id,String msg); }

    static class Portal implements Observer {
        String id;
        Portal(String i) { id=i; }
        public void update(String r,String m) {
            if(r==null || r.equals(id)) System.out.println(m);
        }
    }

    static class Publisher {
        List<Observer> list=new ArrayList<>();
        void add(Observer o) { list.add(o); }
        void remove(Observer o) { list.remove(o); }
        void send(String r,String m) {
            for(Observer o:list) o.update(r,m);
        }
    }

    static class Marks {
        double e,c,p;
        Marks(double e,double c,double p) {
            for(double x:new double[]{e,c,p})
                if(!Double.isFinite(x)||x<0||x>100)
                    throw new IllegalArgumentException();
            this.e=e; this.c=c; this.p=p;
        }
    }

    interface Strategy { double calculate(Marks m); }

    static class Theory implements Strategy {
        public double calculate(Marks m) { return .6*m.e+.4*m.c; }
    }

    static class Lab implements Strategy {
        public double calculate(Marks m) {
            return .3*m.e+.2*m.c+.5*m.p;
        }
    }

    static class Evaluation {
        Strategy s;
        Evaluation(Strategy s) { this.s=s; }
        void change(Strategy x) { s=x; }
        double result(Marks m) { return s.calculate(m); }
    }

    static class ExternalLibrary {
        String copies(String id) {
            if(id.equals("BOOK1")) return "2";
            if(id.equals("BOOK2")) return "0";
            return "UNKNOWN";
        }
    }

    interface Library { boolean available(String id); }

    static class LibraryAdapter implements Library {
        ExternalLibrary l=new ExternalLibrary();
        public boolean available(String id) {
            String n=l.copies(id);
            if(n.equals("UNKNOWN")) throw new IllegalArgumentException();
            return Integer.parseInt(n)>0;
        }
    }

    public static void main(String[] args) {
        Student s=(Student)AccountFactory.create("STUDENT","S1");

        Registration r=new Registration();
        r.register(s,new Course("C1",3,10));

        Publisher p=new Publisher();
        p.add(new Portal("S1"));
        p.add(new Portal("S2"));
        p.send(null,"Campus Notice");

        Marks m=new Marks(80,70,90);
        Evaluation e=new Evaluation(new Theory());
        System.out.println(e.result(m));

        e.change(new Lab());
        System.out.println(e.result(m));

        p.send("S1","Personal Result");

        Library l=new LibraryAdapter();
        System.out.println(l.available("BOOK1"));
    }
}
