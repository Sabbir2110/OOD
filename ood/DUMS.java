
import java.util.*;

public class DUMS {

    // 1. Singleton
    static class UrbanDatabase {
        static UrbanDatabase obj = new UrbanDatabase();
        Map<String, Complaint> data = new HashMap<>();
        private UrbanDatabase() {}
        static UrbanDatabase getInstance() { return obj; }
    }

    static class Complaint {
        String id, citizen, status = "OPEN";
        Complaint(String i, String c) { id=i; citizen=c; }
    }

    // 2. Factory
    interface UrbanService {}
    static class WasteService implements UrbanService {}
    static class TrafficService implements UrbanService {}
    static class EmergencyService implements UrbanService {}

    static class UrbanServiceFactory {
        static UrbanService create(String t) {
            switch(t) {
                case "WASTE": return new WasteService();
                case "TRAFFIC": return new TrafficService();
                case "EMERGENCY": return new EmergencyService();
                default: throw new IllegalArgumentException("Invalid type");
            }
        }
    }

    // 3. Observer
    interface Observer { void update(Complaint c); }

    static class CitizenNotifier implements Observer {
        public void update(Complaint c) {
            System.out.println("Citizen notified: "+c.status);
        }
    }

    static class GovernmentDashboard implements Observer {
        public void update(Complaint c) {
            System.out.println("Dashboard: "+c.status);
        }
    }

    static class Subject {
        List<Observer> list = new ArrayList<>();
        void add(Observer o) { list.add(o); }
        void remove(Observer o) { list.remove(o); }
        void notifyAll(Complaint c) {
            for(Observer o:list) o.update(c);
        }
    }

    // 4. Strategy
    static class Route {
        String id;
        double km, min;
        Route(String i,double k,double m) {
            id=i; km=k; min=m;
        }
    }

    interface RouteStrategy {
        Route select(List<Route> r);
    }

    static class ShortestRouteStrategy implements RouteStrategy {
        public Route select(List<Route> r) {
            return r.stream().min(Comparator.comparingDouble(x->x.km)).orElseThrow();
        }
    }

    static class FastestRouteStrategy implements RouteStrategy {
        public Route select(List<Route> r) {
            return r.stream().min(Comparator.comparingDouble(x->x.min)).orElseThrow();
        }
    }

    static class RoutePlanner {
        RouteStrategy strategy;
        void setStrategy(RouteStrategy s) { strategy=s; }
        Route plan(List<Route> r) {
            if(r.isEmpty()) throw new IllegalArgumentException("No route");
            return strategy.select(r);
        }
    }

    // 5. Adapter
    interface TrafficServiceAPI {
        double getKm();
        double getMinutes();
    }

    static class TrafficAPIAdapter implements TrafficServiceAPI {
        public double getKm() { return 5000/1000.0; }
        public double getMinutes() { return 1800/60.0; }
    }

    // Main
    public static void main(String[] args) {

        UrbanDatabase db = UrbanDatabase.getInstance();
        System.out.println(db==UrbanDatabase.getInstance());

        System.out.println(UrbanServiceFactory.create("WASTE"));

        Subject s = new Subject();
        Observer citizen = new CitizenNotifier();
        Observer dashboard = new GovernmentDashboard();
        s.add(citizen);
        s.add(dashboard);

        Complaint c = new Complaint("101","C1");
        if(c.id.isBlank() || db.data.containsKey(c.id))
            throw new IllegalArgumentException("Invalid ID");
        db.data.put(c.id,c);

        TrafficServiceAPI api = new TrafficAPIAdapter();
        System.out.println(api.getKm()+" km");
        System.out.println(api.getMinutes()+" min");

        List<Route> routes = Arrays.asList(
            new Route("A",5,30),
            new Route("B",8,20),
            new Route("C",6.5,25));

        RoutePlanner p = new RoutePlanner();
        p.setStrategy(new ShortestRouteStrategy());
        System.out.println(p.plan(routes).id);

        p.setStrategy(new FastestRouteStrategy());
        System.out.println(p.plan(routes).id);

        // Dispatch
        UrbanServiceFactory.create("WASTE");
        if(c.status.equals("OPEN") && !routes.isEmpty()) {
            c.status="IN_PROGRESS";
            s.notifyAll(c);
        }

        // Unsubscribe and resolve
        s.remove(dashboard);
        if(c.status.equals("IN_PROGRESS")) {
            c.status="RESOLVED";
            s.notifyAll(c);
        }
    }
}
