package sf;

public class SlotType {
	public static String[][] perSlotTypes = {{"per:alternate_names","Name","List"},
			{"per:date_of_birth","Value","Single"},
			{"per:age","Value","Single"},
			{"per:country_of_birth","Name","Single"},
			{"per:stateorprovince_of_birth","Name","Single"},
			{"per:city_of_birth","Name","Single"},
			{"per:origin","Name","List"},
			{"per:date_of_death","Value","Single"},
			{"per:country_of_death","Name","Single"},
			{"per:stateorprovince_of_death","Name","Single"},
			{"per:city_of_death","Name","Single"},
			{"per:cause_of_death","String","Single"},
			{"per:countries_of_residence","Name","List"},
			{"per:stateorprovinces_of_residence","Name","List"},
			{"per:cities_of_residence","Name","List"},
			{"per:schools_attended","Name","List"},
			{"per:title","String","List"},
			{"per:member_of","Name","List"},
			{"per:employee_of","Name","List"},
			{"per:religion","String","Single"},
			{"per:spouse","Name","List"},
			{"per:children","Name","List"},
			{"per:parents","Name","List"},
			{"per:siblings","Name","List"},
			{"per:other_family","Name","List"},
			{"per:charges","Name","List"}};
	public static String[][] orgSlotTypes = {{"org:alternate_names","Name","List"},
			{"org:political/religious_affiliation","Name","List"},
			{"org:top_members/employees","Name","List"},
			{"org:number_of_employees/members","Value","Single"},
			{"org:members","Name","List"},
			{"org:member_of","Name","List"},
			{"org:subsidiaries","Name","List"},
			{"org:parents","Name","List"},
			{"org:founded_by","Name","List"},
			{"org:founded","Value","Single"},
			{"org:dissolved","Value","Single"},
			{"org:country_of_headquarters","Name","Single"},
			{"org:stateorprovince_of_headquarters","Name","Single"},
			{"org:city_of_headquarters","Name","Single"},
			{"org:shareholders","Name","List"},
			{"org:website","String","Single"}};
	public static boolean isValidSlot(String slot) {
		for (int i = 0; i < SlotType.orgSlotTypes.length; i++) {
			if (SlotType.orgSlotTypes[i][0].equals(slot)) {
				return true;
			}
		}
		for (int i = 0; i < SlotType.perSlotTypes.length; i++) {
			if (SlotType.perSlotTypes[i][0].equals(slot)) {
				return true;
			}
		}
		return false;
	}
}
