/*
 * generated by Xtext
 */
package fr.lip6.move.gal;

import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;
import org.eclipse.xtext.serializer.tokens.ICrossReferenceSerializer;

import com.google.inject.Binder;

import fr.lip6.move.gal.scoping.GalLogicScopeProvider;
import fr.lip6.move.gal.scoping.LogicCrossReferenceSerializer;
import fr.lip6.move.gal.scoping.LogicQualifiedNameProvider;
import fr.lip6.move.scoping.GALCrossReferenceSerializer;
import fr.lip6.move.scoping.GalNameConverter;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class LogicRuntimeModule extends fr.lip6.move.gal.AbstractLogicRuntimeModule {

	@Override
	public Class<? extends IScopeProvider> bindIScopeProvider() {
		return GalLogicScopeProvider.class;
	}
	
	@Override
	public Class<? extends IGlobalScopeProvider> bindIGlobalScopeProvider() {
		return ImportUriGlobalScopeProvider.class;
	}
	
	@Override
	public void configureSerializerIScopeProvider(Binder binder) {
		binder.bind(org.eclipse.xtext.scoping.IScopeProvider.class).annotatedWith(org.eclipse.xtext.serializer.tokens.SerializerScopeProviderBinding.class).to(GalLogicScopeProvider.class);
	}
	
	
	@Override
	public Class<? extends IQualifiedNameConverter> bindIQualifiedNameConverter() {
		return GalNameConverter.class;
	}
	
	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return LogicQualifiedNameProvider.class;
	}

	public Class<? extends ICrossReferenceSerializer> bindICrossRefererenceSerializer() {
		return LogicCrossReferenceSerializer.class;
	}
	
}
