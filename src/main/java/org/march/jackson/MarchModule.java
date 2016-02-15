package org.march.jackson;

import org.march.data.model.Command;
import org.march.data.model.Constant;
import org.march.data.model.Data;
import org.march.sync.channel.ChangeSet;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class MarchModule extends SimpleModule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		
		context.setMixInAnnotations(Data.class, DataMixin.class);
		context.setMixInAnnotations(Constant.class, ConstantMixin.class);
		context.setMixInAnnotations(Command.class, CommandMixin.class);
		
		context.setMixInAnnotations(ChangeSet.class, MessageMixin.class);
	}

	
}
