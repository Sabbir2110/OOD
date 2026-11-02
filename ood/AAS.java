
import java.util.*;

interface Sensor {
    double read();
}

class SoilMoistureSensor implements Sensor {
    public double read() { return 30; }
}

class TemperatureSensor implements Sensor {
    public double read() { return 28; }
}

class SensorFactory {
    static Sensor create(String type) {
        switch(type.toLowerCase()) {
            case "soil": return new SoilMoistureSensor();
            case "temperature": return new TemperatureSensor();
            default: throw new IllegalArgumentException("Invalid sensor");
        }
    }
}

class FarmDataManager {
    private static final FarmDataManager obj = new FarmDataManager();
    private FarmDataManager() {}
    static FarmDataManager getInstance() { return obj; }
}

interface Observer {
    void update(String msg);
}

class FarmerNotifier implements Observer {
    public void update(String msg) {
        System.out.println("Farmer: " + msg);
    }
}

class FarmDashboard implements Observer {
    public void update(String msg) {
        System.out.println("Dashboard: " + msg);
    }
}

class AlertPublisher {
    List<Observer> observers = new ArrayList<>();

    void subscribe(Observer o) { observers.add(o); }
    void unsubscribe(Observer o) { observers.remove(o); }

    void notifyAllObservers(String msg) {
        for(Observer o : observers) o.update(msg);
    }
}

interface IrrigationStrategy {
    boolean shouldIrrigate(double moisture, boolean rain);
}

class NormalIrrigationStrategy implements IrrigationStrategy {
    public boolean shouldIrrigate(double m, boolean r) {
        return m < 40 && !r;
    }
}

class WaterSavingStrategy implements IrrigationStrategy {
    public boolean shouldIrrigate(double m, boolean r) {
        return m < 25 && !r;
    }
}

class ExternalWeather {
    boolean getRainStatus() { return false; }
}

interface WeatherService {
    boolean isRainExpected();
}

class WeatherAPIAdapter implements WeatherService {
    ExternalWeather weather = new ExternalWeather();
    public boolean isRainExpected() {
        return weather.getRainStatus();
    }
}

class IrrigationController {
    IrrigationStrategy strategy;
    WeatherService weather = new WeatherAPIAdapter();
    AlertPublisher publisher;

    IrrigationController(IrrigationStrategy s, AlertPublisher p) {
        strategy = s;
        publisher = p;
    }

    void check(double moisture) {
        if(moisture < 0 || moisture > 100) {
            System.out.println("Invalid reading");
            return;
        }

        boolean on = strategy.shouldIrrigate(
            moisture, weather.isRainExpected());

        System.out.println(on ? "Pump ON" : "Pump OFF");
        publisher.notifyAllObservers(on ?
            "Irrigation started" : "Irrigation not required");
    }

    void setStrategy(IrrigationStrategy s) {
        strategy = s;
    }
}

public class Main {
    public static void main(String[] args) {

        FarmDataManager a = FarmDataManager.getInstance();
        FarmDataManager b = FarmDataManager.getInstance();
        System.out.println(a == b);

        Sensor soil = SensorFactory.create("soil");
        Sensor temp = SensorFactory.create("temperature");
        System.out.println(soil.read());
        System.out.println(temp.read());

        AlertPublisher p = new AlertPublisher();
        Observer f = new FarmerNotifier();
        Observer d = new FarmDashboard();

        p.subscribe(f);
        p.subscribe(d);

        IrrigationController c =
            new IrrigationController(new NormalIrrigationStrategy(), p);

        c.check(30);
        c.setStrategy(new WaterSavingStrategy());
        c.check(30);
        c.check(20);
        c.check(40);
        c.check(25);
        c.check(-5);
        c.check(105);

        p.unsubscribe(d);
        p.notifyAllObservers("Test alert");
    }
}
