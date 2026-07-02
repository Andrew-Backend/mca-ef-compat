package forge.net.mca.entity.ai;

import forge.net.mca.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;

public interface SchedulesMCA {
   Schedule DEFAULT = new ScheduleBuilder(new Schedule())
      .m_38040_(10, Activity.f_37979_)
      .m_38040_(2000, Activity.f_37980_)
      .m_38040_(9000, Activity.f_37983_)
      .m_38040_(11000, Activity.f_37979_)
      .m_38040_(12500, Activity.f_37982_)
      .m_38039_();
   Schedule NIGHT_OWL_DEFAULT = new ScheduleBuilder(new Schedule())
      .m_38040_(10, Activity.f_37982_)
      .m_38040_(12500, Activity.f_37979_)
      .m_38040_(15000, Activity.f_37980_)
      .m_38040_(18000, Activity.f_37983_)
      .m_38040_(19500, Activity.f_37979_)
      .m_38039_();
   Schedule GUARD = new ScheduleBuilder(new Schedule())
      .m_38040_(10, Activity.f_37980_)
      .m_38040_(9000, Activity.f_37983_)
      .m_38040_(11000, Activity.f_37980_)
      .m_38040_(14000, Activity.f_37979_)
      .m_38040_(15000, Activity.f_37982_)
      .m_38039_();
   Schedule GUARD_NIGHT = new ScheduleBuilder(new Schedule())
      .m_38040_(10, Activity.f_37982_)
      .m_38040_(8000, Activity.f_37979_)
      .m_38040_(9000, Activity.f_37983_)
      .m_38040_(14000, Activity.f_37980_)
      .m_38039_();
   Schedule GUESTS = new ScheduleBuilder(new Schedule()).m_38040_(10, Activity.f_37979_).m_38040_(12500, Activity.f_37982_).m_38039_();

   static void bootstrap() {
   }

   static Schedule getTypeSchedule(LivingEntity entity, boolean allowNightOwl, Schedule normalSchedule, Schedule nightSchedule) {
      return allowNightOwl && entity.m_217043_().m_188501_() < Config.getInstance().nightOwlChance ? nightSchedule : normalSchedule;
   }

   static Schedule getTypeSchedule(LivingEntity entity, Schedule normalSchedule, Schedule nightSchedule) {
      return getTypeSchedule(entity, Config.getInstance().allowAnyNightOwl, normalSchedule, nightSchedule);
   }

   static Schedule getTypeSchedule(LivingEntity entity, boolean allowNightOwl) {
      return getTypeSchedule(entity, allowNightOwl, DEFAULT, NIGHT_OWL_DEFAULT);
   }

   static Schedule getTypeSchedule(LivingEntity entity) {
      return getTypeSchedule(entity, Config.getInstance().allowAnyNightOwl);
   }
}
