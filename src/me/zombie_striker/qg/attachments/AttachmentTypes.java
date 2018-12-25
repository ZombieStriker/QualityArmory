package me.zombie_striker.qg.attachments;

public enum AttachmentTypes {

	SILENCER("Silencer"),EXTENDEDMAGS("Extended_Mags"),PAINT("Skin"),JOKE_ITEM("Joke");
	String name;
	AttachmentTypes(String name) {
	this.name= name;
	}
	
	public AttachmentTypes getAttachByName(String name) {
		for(AttachmentTypes a : AttachmentTypes.values()) {
			if(a.name.equals(name))
				return a;
		}
		return null;
	}
}
