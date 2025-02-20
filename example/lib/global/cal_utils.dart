enum Gender { MALE, FEMALE}

extension GenderExtension on Gender {
  String get name {
    return ["MALE", "FEMALE"][this.index];
  }
}

/*class CalUtil{

  double height = 173;// in cm
  double weight = 72;// in Kg

  DateTime dob = DateTime(1992, 2, 27, 05, 45, 0);

  String gender = Gender.MALE.name;


  void initializePersonalData(){
    double mHeightInMeters = centimeterToMeters(height);
    int age = getAgeFromDateOfBirth(dob);
  }

}*/

//Based on https://riggaroo.co.za/calories-burnt-calculation-for-walkingrunning-in-java/

///Calculated the energy expenditure for an activity.
///
///@param height               The height in metres.
///@param age                  The date of birth.
///@param weight               The weight of the user.
///@param gender               The gender of the user.
///@param durationInSeconds    The duration of the activity in seconds.
///@param stepsTaken           The steps taken.
///@param strideLengthInMetres The stride length of the user
///@return The number of calories burnt (kCal)

// double calculateEnergyExpenditure(double height, DateTime age, double weight, String gender, int durationInSeconds, int stepsTaken) {
//
//   int ageCalculated = getAgeFromDateOfBirth(age);
//
//   double harrisBenedictRmR = convertKilocaloriesToMlKmin(harrisBenedictRmr(gender, weight, ageCalculated, height), weight);
//   double calculatedStrideLengthInMeters = calculateStepToMeters(stepsTaken, height, gender);
//   double kmTravelled = calculateDistanceTravelledInKM(stepsTaken, calculatedStrideLengthInMeters);
//   double hours = secondsToHours(durationInSeconds);
//   double speedInMph = kilometersToMiles(kmTravelled) / hours;
//   double metValue = getMetForActivity(speedInMph);
//   double constant = 3.5;
//   double correctedMets = metValue * (constant / harrisBenedictRmR);
//   return correctedMets * hours * weight;
// }

// changed DateTime age into int ageCalculated
double calculateEnergyExpenditure(double height, int ageCalculated, double weight,
    String gender, int durationInSeconds,int stepsTaken) {
  // int ageCalculated = getAgeFromDateOfBirth(age);
  double harrisBenedictRmR = convertKilocaloriesToMlKmin(harrisBenedictRmr(gender, weight, ageCalculated, height), weight);
  double calculatedStrideLengthInMeters = calculateStepToMeters(stepsTaken, height, gender);
  double kmTravelled = calculateDistanceTravelledInKM(stepsTaken, calculatedStrideLengthInMeters);
  double hours = secondsToHours(durationInSeconds);
  double speedInMph = kilometersToMiles(kmTravelled) / hours;
  double metValue = getMetForActivity(speedInMph);
  double constant = 3.5;
  double correctedMets = metValue * (constant / harrisBenedictRmR);
  return correctedMets * hours * weight;
}

String getDistanceWithSteps(double totalSteps){
  return (totalSteps * 0.000762).toStringAsFixed(2);
}
/// Gets a users age from a date. Only takes into account years.
///
/// @param age The date of birth.
/// @return The age in years.
int getAgeFromDateOfBirth(DateTime age) {
  DateTime currentDate = DateTime.now();
  DateTime dateOfBirth = age;

  if (dateOfBirth.compareTo(currentDate) > 0) {
    throw ("Can't be born in the future");
  }
  int currentYear = currentDate.year;
  int dateOfBirthYear = dateOfBirth.year;
  int age2 = currentYear - dateOfBirthYear;
  int currentMonth = currentDate.month;
  int dateOfBirthMonth = dateOfBirth.month;

  if (dateOfBirthMonth > currentMonth) {
    age2--;
  } else if (currentMonth == dateOfBirthMonth) {
    int currentDay = currentDate.day;
    int dateOfBirthDay = dateOfBirth.day;
    if (dateOfBirthDay > currentDay) {
      age2--;
    }
  }
  return age2;
}

double convertKilocaloriesToMlKmin(double kilocalories, double weightKgs) {
  double kcalMin = kilocalories / 1440;
  kcalMin /= 5;

  return ((kcalMin / (weightKgs)) * 1000);
}

double calculateDistanceTravelledInKM(int stepsTaken, double entityStrideLength) {
  return ((stepsTaken * entityStrideLength) / 1000);
}

///Gets the MET value for an activity.
///
///@param speedInMph The speed in miles per hour
///@return The met value.
double getMetForActivity(double speedInMph) {
  if (speedInMph < 2.0) {
    return 2.0;
  } else if (speedInMph.compareTo(2.0) == 0) {
    return 2.8;
  } else if (speedInMph.compareTo(2.0) > 0 && speedInMph.compareTo(2.7) <= 0) {
    return 3.0;
  } else if (speedInMph.compareTo(2.8) > 0 && speedInMph.compareTo(3.3) <= 0) {
    return 3.5;
  } else if (speedInMph.compareTo(3.4) > 0 && speedInMph.compareTo(3.5) <= 0) {
    return 4.3;
  } else if (speedInMph.compareTo(3.5) > 0 && speedInMph.compareTo(4.0) <= 0) {
    return 5.0;
  } else if (speedInMph.compareTo(4.0) > 0 && speedInMph.compareTo(4.5) <= 0) {
    return 7.0;
  } else if (speedInMph.compareTo(4.5) > 0 && speedInMph.compareTo(5.0) <= 0) {
    return 8.3;
  } else if (speedInMph.compareTo(5.0) > 0) {
    return 9.8;
  }
  return 0;
}

///Calculates the Harris Benedict RMR value for an entity. Based on above calculation for Com
///
///@param gender   Users gender.
///@param weightKg Weight in Kg.
///@param age      Age in years.
///@param heightCm Height in CM.
///@return Harris benedictRMR value.
double harrisBenedictRmr(String gender, double weightKg, int age, double heightCm) {

  if (gender == Gender.FEMALE.name) {
    return 655.0955 +
        (1.8496 * heightCm) +
        (9.5634 * weightKg) -
        (4.6756 * age);
  } else {
    return 66.4730 +
        (5.0033 * heightCm) +
        (13.7516 * weightKg) -
        (6.7550 * age);
  }
}

double secondsToHours(int seconds) {
  return seconds / 60 / 60;
}

double kilometersToMiles(double kilometers) {
  return kilometers * 0.62137;
}

double centimeterToMeters(double cm) {
  return cm / 100;
}

double meterToCentimer(double m) {
  return m * 100;
}

double calculateStepToMeters(int steps, double heightInMeters, String gender) {
//https://www.wikihow.com/Measure-Stride-Length
  return steps * centimeterToMeters(gender == Gender.FEMALE.name ? 0.413 : 0.415 * meterToCentimer(heightInMeters));
}
