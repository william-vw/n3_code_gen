class TreatmentSubplan {
	static lifestyleSubplan = 'lifestyleSubplan';

	label;
	type;
}

class DiabetesPhysicalExamination {
	static bmi = 'bmi';

	isPhysicalExaminationOf;
	type;
	hasQuantitativeValue;
}

class DiabetesMellitus {
	static type2DiabetesMellitus = 'type2DiabetesMellitus';

	type;
}

class DiabetesDiagnosis {
	hasDiabetesType;
	type;
}

class TreatmentPlan {
	hasPart = [];
	type;
}

class Patient {
	hasPatientProfile;
	type;
}

class PatientProfile {
	isPatientProfileOf;
	hasDemographic;
	hasTreatmentPlan;
	hasDiagnosis;
	type;
}

class PatientDemographic {
	static overweight = 'overweight';

	type;
}

function doSomething(exam, p) {

	if (exam.hasQuantitativeValue !== null
		&& exam.hasQuantitativeValue >= 25
		&& exam.type == DiabetesPhysicalExamination.bmi) {

		var v0 = new PatientDemographic();
		exam.isPhysicalExaminationOf.hasDemographic = v0;
		v0.type = PatientDemographic.overweight;
	}

	if (p.hasPatientProfile !== null
		&& p.hasPatientProfile.hasDiagnosis !== null
		&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType !== null
		&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitus.type2DiabetesMellitus
		&& p.hasPatientProfile.hasDemographic !== null
		&& p.hasPatientProfile.hasDemographic.type == PatientDemographic.overweight) {

		var v1 = new TreatmentPlan();
		p.hasPatientProfile.hasTreatmentPlan = v1;
		var v2 = new TreatmentSubplan();
		v1.hasPart = v2;
		v2.label = "Management and reduction of weight is important";
		v2.type = TreatmentSubplan.lifestyleSubplan;
	}
}

