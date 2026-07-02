package quilt.net.mca.entity.ai;

import net.minecraft.class_1309;
import net.minecraft.class_4168;
import net.minecraft.class_4170;
import net.minecraft.class_4171;
import quilt.net.mca.Config;

public interface SchedulesMCA {
   class_4170 DEFAULT = new class_4171(new class_4170())
      .method_19221(10, class_4168.field_18595)
      .method_19221(2000, class_4168.field_18596)
      .method_19221(9000, class_4168.field_18598)
      .method_19221(11000, class_4168.field_18595)
      .method_19221(12500, class_4168.field_18597)
      .method_19220();
   class_4170 NIGHT_OWL_DEFAULT = new class_4171(new class_4170())
      .method_19221(10, class_4168.field_18597)
      .method_19221(12500, class_4168.field_18595)
      .method_19221(15000, class_4168.field_18596)
      .method_19221(18000, class_4168.field_18598)
      .method_19221(19500, class_4168.field_18595)
      .method_19220();
   class_4170 GUARD = new class_4171(new class_4170())
      .method_19221(10, class_4168.field_18596)
      .method_19221(9000, class_4168.field_18598)
      .method_19221(11000, class_4168.field_18596)
      .method_19221(14000, class_4168.field_18595)
      .method_19221(15000, class_4168.field_18597)
      .method_19220();
   class_4170 GUARD_NIGHT = new class_4171(new class_4170())
      .method_19221(10, class_4168.field_18597)
      .method_19221(8000, class_4168.field_18595)
      .method_19221(9000, class_4168.field_18598)
      .method_19221(14000, class_4168.field_18596)
      .method_19220();
   class_4170 GUESTS = new class_4171(new class_4170()).method_19221(10, class_4168.field_18595).method_19221(12500, class_4168.field_18597).method_19220();

   static void bootstrap() {
   }

   static class_4170 getTypeSchedule(class_1309 entity, boolean allowNightOwl, class_4170 normalSchedule, class_4170 nightSchedule) {
      return allowNightOwl && entity.method_6051().method_43057() < Config.getInstance().nightOwlChance ? nightSchedule : normalSchedule;
   }

   static class_4170 getTypeSchedule(class_1309 entity, class_4170 normalSchedule, class_4170 nightSchedule) {
      return getTypeSchedule(entity, Config.getInstance().allowAnyNightOwl, normalSchedule, nightSchedule);
   }

   static class_4170 getTypeSchedule(class_1309 entity, boolean allowNightOwl) {
      return getTypeSchedule(entity, allowNightOwl, DEFAULT, NIGHT_OWL_DEFAULT);
   }

   static class_4170 getTypeSchedule(class_1309 entity) {
      return getTypeSchedule(entity, Config.getInstance().allowAnyNightOwl);
   }
}
