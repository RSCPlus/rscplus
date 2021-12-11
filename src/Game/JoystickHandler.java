package Game;

import Client.Settings;
import Client.Launcher;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JoystickHandler {
    public static List<String> knownAxises = new ArrayList<>();
    public static HashMap<String, Float> joystickInputReports = new HashMap<String, Float>();
    public static HashMap<String, Long> joystickInputReportTimestamps = new HashMap<String, Long>();

    public static void init() {
        System.setProperty("net.java.games.input.librarypath", new File(Settings.Dir.JAR + "/lib/jinput-natives").getAbsolutePath());
    }
    public static void poll() {
        /* Get the available controllers */
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        if (controllers.length == 0) {
            return;
        }

        for (int i = 0; i < controllers.length; i++) {
            if (!controllers[i].getName().contains("SpaceNavigator"))
                continue;

            // process the controller's eventqueue
            controllers[i].poll();
            EventQueue queue = controllers[i].getEventQueue();
            Event event = new Event();
            while (queue.getNextEvent(event)) {
                Component comp = event.getComponent();
                if (!knownAxises.contains(comp.getName())) {
                    knownAxises.add(comp.getName());
                }

                float value = event.getValue();
                joystickInputReports.put(comp.getName(), value);
                joystickInputReportTimestamps.put(comp.getName(), System.currentTimeMillis());

                doJoystickAction(comp.getName());
            }

            // Update inputs that are stale
            // returning to Zero position does not consistently generate an event, so we do this.
            joystickInputReportTimestamps.forEach((key, value) -> {
                if (value != 0) {
                    if (System.currentTimeMillis() - value > 100) {
                        joystickInputReports.put(key, 0f);
                        doJoystickAction(key);
                    }
                }
            });
        }
    }

    public static void doJoystickAction(String inputName) {
        if (joystickInputReports.get(inputName) != 0) {
            Camera.handleJoystick(inputName);
        }
        if (Launcher.getConfigWindow().isShown()) {
            Launcher.getConfigWindow().updateJoystickInput(inputName);
        }
    }
}
