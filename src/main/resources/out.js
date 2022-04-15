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
	hasDemographic = [];
	hasTreatmentPlan;
	hasDiagnosis;
	type;
}

class PatientDemographic {
	static overweight = 'overweight';

	type;
}

function doSomething(exam, p) {
	if (exam.hasQuantitativeValue !== undefined
		&& exam.hasQuantitativeValue >= 25
		&& exam.type === DiabetesPhysicalExamination.bmi) {
	
		var v0 = new PatientDemographic();
		exam.isPhysicalExaminationOf.hasDemographic.push(v0);
		
		v0.type = PatientDemographic.overweight;
	}
	
	if (p.hasPatientProfile !== undefined
		&& p.hasPatientProfile.hasDiagnosis !== undefined
		&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType !== undefined
		&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType.type === DiabetesMellitus.type2DiabetesMellitus
		&& p.hasPatientProfile.hasDemographic.some((e) => e.type === PatientDemographic.overweight)) {
	
		if (p.hasPatientProfile.hasTreatmentPlan === undefined) {
			p.hasPatientProfile.hasTreatmentPlan = new TreatmentPlan();
		}
		var v1 = p.hasPatientProfile.hasTreatmentPlan;
		
		var v2 = new TreatmentSubplan();
		v1.hasPart.push(v2);
		
		v2.label = "Management and reduction of weight is important";
		v2.type = TreatmentSubplan.lifestyleSubplan;
	}
}