package com.zyrex.module;

import com.zyrex.module.impl.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<Module>();

    public ModuleManager() {
        modules.add(new KillAuraModule());
        modules.add(new AutoClickerModule());
        modules.add(new CritModule());
        modules.add(new VelocityModule());
        modules.add(new TriggerBotModule());
        modules.add(new AntiBotModule());
        modules.add(new InfiniteReachModule());
        modules.add(new FlightModule());
        modules.add(new SpeedModule());
        modules.add(new NoFallModule());
        modules.add(new NoSlowModule());
        modules.add(new SprintModule());
        modules.add(new ScaffoldModule());
        modules.add(new FastPlaceModule());
        modules.add(new PhaseModule());
        modules.add(new MoveFixModule());
        modules.add(new AntiVoidModule());
        modules.add(new OmnidirectionalSprintModule());
        modules.add(new BreakerModule());
        modules.add(new RegenModule());
        modules.add(new FreeLookModule());
        modules.add(new StaffDetectorModule());
        modules.add(new AnticheatDetectionModule());
        modules.add(new DisablerModule());
        modules.add(new FakeLagModule());
        modules.add(new InfiniteMineModule());
        modules.add(new BedBreakerModule());
        modules.add(new TeleportAuraModule());
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> list = new ArrayList<Module>();
        for (Module m : modules) {
            if (m.getCategory() == category) list.add(m);
        }
        return list;
    }
}
