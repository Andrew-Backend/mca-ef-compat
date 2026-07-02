package yesman.epicfight.api.utils;

public interface CirculatableEnum<T extends Enum<T>> {
   T nextEnum();
}
