// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot {
	// Declares a state variable `message` of type `string`.
	// State variables are variables whose values are permanently stored in contract storage. The keyword `public` makes variables accessible from outside a contract and creates a function that other contracts or clients can call to access the value.
	string public message;
	
	// Similar to many class-based object-oriented languages, a constructor is a special function that is only executed upon contract creation.
	// Constructors are used to initialize the contract's data. Learn more:https://solidity.readthedocs.io/en/v0.5.10/contracts.html#constructors
	constructor(string memory initMessage) {
	
		// Accepts a string argument `initMessage` and sets the value into the contract's `message` storage variable).
		message = initMessage;
	}
	
	enum TreatmentSubplanConstants{ LifestyleSubplan }
	
	struct TreatmentSubplan {
		string label;
		TreatmentSubplanConstants type;
		bool exists;
	}
	
	enum DiabetesPhysicalExaminationConstants{ Bmi }
	
	struct DiabetesPhysicalExamination {
		PatientProfile isPhysicalExaminationOf;
		DiabetesPhysicalExaminationConstants type;
		int hasQuantitativeValue;
		bool exists;
	}
	
	enum DiabetesMellitusConstants{ Type2DiabetesMellitus }
	
	struct DiabetesMellitus {
		DiabetesMellitusConstants type;
		bool exists;
	}
	
	struct TreatmentPlan {
		mapping(TreatmentSubplanConstants => TreatmentSubplan) hasPart;
		bool exists;
	}
	
	struct DiabetesDiagnosis {
		DiabetesMellitus hasDiabetesType;
		bool exists;
	}
	
	struct Patient {
		PatientProfile hasPatientProfile;
		bool exists;
	}
	
	struct PatientProfile {
		Patient isPatientProfileOf;
		mapping(PatientDemographicConstants => PatientDemographic) hasDemographic;
		TreatmentPlan hasTreatmentPlan;
		DiabetesDiagnosis hasDiagnosis;
		bool exists;
	}
	
	enum PatientDemographicConstants{ Overweight }
	
	struct PatientDemographic {
		PatientDemographicConstants type;
		bool exists;
	}
	
	function doSomething(exam, p) {
		if (exam.hasQuantitativeValue != false
			&& exam.hasQuantitativeValue >= 25
			&& exam.type == DiabetesPhysicalExaminationConstants.Bmi) {
		
			PatientDemographic memory v0 = PatientDemographic({ exists: true });
			v0.type = PatientDemographicConstants.Overweight;
			exam.isPhysicalExaminationOf.hasDemographic[v0.type] = v0;
		}
		
		if (p.hasPatientProfile != false
			&& p.hasPatientProfile.hasDiagnosis != false
			&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType != false
			&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitusConstants.Type2DiabetesMellitus
			&& p.hasPatientProfile.hasDemographic[PatientDemographicConstants.Overweight].exists) {
		
			if (p.hasPatientProfile.hasTreatmentPlan == false) {
				p.hasPatientProfile.hasTreatmentPlan = TreatmentPlan({ exists: true });
			}
			TreatmentPlan memory v1 = p.hasPatientProfile.hasTreatmentPlan;
			
			TreatmentSubplan memory v2 = TreatmentSubplan({ exists: true });
			v2.label = "Management and reduction of weight is important";
			v2.type = TreatmentSubplanConstants.LifestyleSubplan;
			v1.hasPart[v2.type] = v2;
		}
	}
	
	// A public function that accepts a string argument and updates the `message` storage variable.
	function update(string memory newMessage) public {
		message = newMessage;
	}}