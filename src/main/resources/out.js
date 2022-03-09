class TreatmentSubplan {
	static lifestyleSubplan = 'lifestyleSubplan';

	label;
	type;

}

class DiabetesPhysicalExamination {
	static bmi = 'bmi';

	type;
	hasQuantitativeValue;

}

class DiabetesMellitus {
	static type2DiabetesMellitus = 'type2DiabetesMellitus';

	type;

}

class TreatmentPlan {
	hasPart;
	type;

}

class Patient {
	static patient = 'patient';

	hasPatientProfile;
	type;

}

class PatientProfile {
	hasPhysicalExamination;
	hasDemographic;
	hasTreatmentPlan;
	hasDiagnosis;
	type;

}

class PatientDemographic {
	static overweight = 'overweight';

	type;

}

class Diagnosis {
	hasDiabetesType;
	type;

}

function doSomething(p) {

	if (p.hasPatientProfile !== null && p.hasPatientProfile.hasPhysicalExamination !== null && p.hasPatientProfile.hasPhysicalExamination.hasQuantitativeValue !== null && p.hasPatientProfile.hasPhysicalExamination.hasQuantitativeValue >= 25 && p.hasPatientProfile.hasPhysicalExamination.type == DiabetesPhysicalExamination.bmi && p.type == Patient.patient) {
		var v0 = new PatientDemographic();
		p.hasPatientProfile.hasDemographic = v0;
		v0.type = PatientDemographic.overweight;
	}

	if (p.hasPatientProfile !== null && p.hasPatientProfile.hasDiagnosis !== null && p.hasPatientProfile.hasDiagnosis.hasDiabetesType !== null && p.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitus.type2DiabetesMellitus && p.hasPatientProfile.hasDemographic !== null && p.hasPatientProfile.hasDemographic.type == PatientDemographic.overweight && p.type == Patient.patient) {
		var v1 = new TreatmentPlan();
		p.hasPatientProfile.hasTreatmentPlan = v1;
		var v2 = new TreatmentSubplan();
		v1.hasPart = v2;
		v2.type = TreatmentSubplan.lifestyleSubplan;
		v2.label = "Management and reduction of weight is important";
	}
}

