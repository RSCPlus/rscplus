package Client;

import java.util.HashMap;
import java.util.Map;

public enum RanOverrideEffectType {
  DISABLED(-1),
  VANILLA(0),
  SLOWER(1),
  RGB_WAVE(2),
  STATIC(3),
  FLASH1(6),
  FLASH2(7),
  FLASH3(8),
  GLOW1(9),
  GLOW2(10),
  GLOW3(11);

  private final int effectType;

  RanOverrideEffectType(int changeType) {
    this.effectType = changeType;
  }

  public int id() {
    return effectType;
  }

  private static final Map<Integer, RanOverrideEffectType> byId =
      new HashMap<Integer, RanOverrideEffectType>();

  static {
    for (RanOverrideEffectType changeType : RanOverrideEffectType.values()) {
      if (byId.put(changeType.id(), changeType) != null) {
        throw new IllegalArgumentException("duplicate id: " + changeType.id());
      }
    }
  }

  public static RanOverrideEffectType getById(Integer id) {
    return byId.getOrDefault(id, VANILLA);
  }
}
