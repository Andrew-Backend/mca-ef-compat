# MCA + Epic Fight Compatibility Mod

Исправляет конфликт между **Minecraft Comes Alive 7.6.26** и **Epic Fight 20.14.14** на MC 1.20.1.

## Проблема

MCA изменяет пропорции тела игрока (рост, ширина) через генетику.  
EF отменяет стандартный рендеринг игрока и рисует свой скиннед-меш.  
Из-за этого MCA-масштаб никогда не применяется — в EF-режиме игрок всегда выглядит как стандартный ванильный размер. также не срабатывают анимации EF: руки и ноги не двигаются

## Решение

Мод через Mixin перехватывает `LivingEntityPatch.overrideRender()` в EF и
до начала рендеринга пушит MCA-генетический масштаб на PoseStack. После рендеринга — попает обратно.

---

## Сборка

### Требования
- **JDK 17** (не 21 — Forge 1.20.1 требует именно 17)
- **Git** (опционально)
- Интернет для скачивания Forge MDK и зависимостей

### Шаги

**1. Скачай Forge MDK для 1.20.1**
```
https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.3.0/forge-1.20.1-47.3.0-mdk.zip
```
Распакуй в любую папку, например `forge-mdk/`.

**2. Скопируй исходники этого мода**

Скопируй содержимое этого архива (`mca-ef-compat-mod/`) поверх распакованного MDK.  
Т.е. `build.gradle`, `settings.gradle` и `src/` заменяют стандартные файлы MDK.

**3. Создай папку `libs/` в корне проекта**
```
mkdir libs
```
Скопируй туда оба файла из своего модпака:
```
libs/minecraft-comes-alive-7.6.26+1.20.1-universal.jar
libs/epic-fight-20.14.14-mc1.20.1-forge.jar
```

**4. Собери мод**

Windows:
```bat
gradlew.bat build
```
Linux/Mac:
```bash
chmod +x gradlew
./gradlew build
```

Первый запуск скачает Minecraft и Forge (~500 МБ). Займёт 5–15 минут.

**5. Готовый JAR**

После сборки файл будет здесь:
```
build/libs/mca-ef-compat-1.0.0.jar
```

Скопируй его в папку `mods/` вместе с MCA и Epic Fight.

---

## Структура проекта

```
src/main/java/net/mcaefcompat/
    McaEfCompat.java                      ← главный класс мода (@Mod)
    mixin/
        MixinLivingEntityPatch.java       ← единственный Mixin, вся логика здесь

src/main/resources/
    META-INF/mods.toml                    ← метаданные мода
    mixins.mcaefcompat.json               ← конфиг Mixin
    pack.mcmeta
```

## Как работает (технически)

```
Стандартный рендер без мода:
  RenderLivingEvent$Pre
    → EF: overrideRender() ← рисует свой меш БЕЗ MCA-масштаба
    → EF: event.setCanceled(true)
    → MCA: injectScale() ← НИКОГДА НЕ ЗАПУСКАЕТСЯ (отменено)

С этим модом:
  RenderLivingEvent$Pre
    → EF: overrideRender() HEAD
        → МЫ: poseStack.pushPose() + poseStack.scale(hScale, vScale, hScale)
        → EF: рисует свой меш В ПРАВИЛЬНОМ МАСШТАБЕ
        → МЫ: poseStack.popPose()
    → EF: event.setCanceled(true)
```

## Совместимость

- Minecraft: 1.20.1
- Forge: 47.x
- MCA: 7.6.x
- Epic Fight: 20.14.x
