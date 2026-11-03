
import java.util.*;

public class Main {
    static class UrbanDatabase {
        static final UrbanDatabase obj=new UrbanDatabase();
        Map<String,Complaint> data=new HashMap<>();
        private UrbanDatabase(){}
        static UrbanDatabase get(){return obj;}
    }

    static class Complaint {
        String id,citizen,type,status="OPEN";
        Complaint(String i,String c,String t){
            id=i;citizen=c;type=t;
        }
    }

    interface UrbanService {}
    static class WasteService implements UrbanService {}
    static class TrafficService implements UrbanService {}
    static class EmergencyService implements UrbanService {}

    static class UrbanServiceFactory {
        static UrbanService create(String s){
            switch(s){
                case "WASTE":return new WasteService();
                case "TRAFFIC":return new TrafficService();
                case "EMERGENCY":return new EmergencyService();
                default:throw new RuntimeException("Invalid type");
            }
        }
    }

    interface Observer {
        void update(String id,String msg);
    }

    static class CitizenNotifier implements Observer {
        String id;
        CitizenNotifier(String i){id=i;}
        public void update(String r,String m){
            if(id.equals(r))System.out.println(m);
        }
    }

    static class GovernmentDashboard implements Observer {
        public void update(String r,String m){
            System.out.println("Dashboard: "+m);
        }
    }

    static class Publisher {
        List<Observer> list=new ArrayList<>();
        void add(Observer o){list.add(o);}
        void remove(Observer o){list.remove(o);}
        void send(String r,String m){
            for(Observer o:list)o.update(r,m);
        }
    }

    static class Route {
        String id;
        double km,time;
        Route(String i,double k,double t){
            id=i;km=k;time=t;
        }
    }

    interface Strategy {
        Route select(List<Route> r);
    }

    static class Shortest implements Strategy {
        public Route select(List<Route> r){
            return r.stream().min(
                Comparator.comparingDouble((Route x)->x.km)
                .thenComparing(x->x.id)).orElseThrow();
        }
    }

    static class Fastest implements Strategy {
        public Route select(List<Route> r){
            return r.stream().min(
                Comparator.comparingDouble((Route x)->x.time)
                .thenComparing(x->x.id)).orElseThrow();
        }
    }

    static class RoutePlanner {
        Strategy s;
        RoutePlanner(Strategy s){this.s=s;}
        void set(Strategy x){s=x;}
        Route plan(List<Route> r){return s.select(r);}
    }

    static class ExternalTrafficAPI {
        double[][] get(){
            return new double[][]{
                {5000,1800},{8000,1200},{6500,1500}
            };
        }
    }

    interface TrafficServiceAPI {
        List<Route> getRoutes();
    }

    static class TrafficAPIAdapter implements TrafficServiceAPI {
        public List<Route> getRoutes(){
            List<Route> r=new ArrayList<>();
            double[][] d=new ExternalTrafficAPI().get();
            for(int i=0;i<d.length;i++)
                r.add(new Route(""+(char)('A'+i),
                    d[i][0]/1000,d[i][1]/60));
            return r;
        }
    }

    static class ComplaintController {
        UrbanDatabase db=UrbanDatabase.get();
        Publisher p;
        RoutePlanner planner;

        ComplaintController(Publisher p,RoutePlanner r){
            this.p=p;planner=r;
        }

        void submit(String id,String citizen,String type){
            if(id==null||id.isBlank()||citizen==null||citizen.isBlank()
                ||db.data.containsKey(id))throw new RuntimeException();
            UrbanServiceFactory.create(type);
            db.data.put(id,new Complaint(id,citizen,type));
        }

        void dispatch(String id,List<Route> routes){
            Complaint c=db.data.get(id);
            if(c==null||!c.status.equals("OPEN"))throw new RuntimeException();
            UrbanServiceFactory.create(c.type);
            Route r=planner.plan(routes);
            c.status="IN_PROGRESS";
            p.send(c.citizen,"Route "+r.id+" IN_PROGRESS");
        }

        void resolve(String id){
            Complaint c=db.data.get(id);
            if(c==null||!c.status.equals("IN_PROGRESS"))
                throw new RuntimeException();
            c.status="RESOLVED";
            p.send(c.citizen,"RESOLVED");
        }
    }

    public static void main(String[] args){
        System.out.println(UrbanDatabase.get()==UrbanDatabase.get());

        Publisher p=new Publisher();
        CitizenNotifier c=new CitizenNotifier("C1");
        GovernmentDashboard g=new GovernmentDashboard();
        p.add(c);p.add(g);

        RoutePlanner r=new RoutePlanner(new Shortest());
        ComplaintController cc=new ComplaintController(p,r);
        TrafficServiceAPI api=new TrafficAPIAdapter();

        cc.submit("1","C1","WASTE");
        cc.dispatch("1",api.getRoutes());
        r.set(new Fastest());
        System.out.println(r.plan(api.getRoutes()).id);

        p.remove(g);
        cc.resolve("1");

        System.out.println(api.getRoutes().get(0).km);
        System.out.println(api.getRoutes().get(0).time);
    }
}
