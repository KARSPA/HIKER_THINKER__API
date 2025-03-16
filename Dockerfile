FROM openjdk:17
ADD build/libs/HIKER_THINKER-0.0.1-SNAPSHOT.jar hkapp.jar
ENTRYPOINT ["java", "-jar", "hkapp.jar"]