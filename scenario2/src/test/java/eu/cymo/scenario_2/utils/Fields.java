package eu.cymo.scenario_2.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Fields {

	private Fields() {}
	
	public static <T, R extends Annotation> T getAnnotationValue(Field field, Class<R> annotation, Function<R, T> property) {
		return property.apply(field.getAnnotation(annotation));
	}
	
	public static Type[] getParemeterizdTypes(Field field) {
		var genericType = field.getGenericType();
		if(genericType instanceof ParameterizedType parameterizedType) {
			return parameterizedType.getActualTypeArguments();
		}
		return null;
	}

	public static List<Field> getAllFields(Class<?> cl) {
		if(cl == null || cl == Object.class) {
			return Collections.emptyList();
		}
		
		return Stream.concat(
					Arrays.stream(cl.getDeclaredFields()),
					getAllFields(cl.getEnclosingClass()).stream())
				.toList();
	}
	
	public static Object getFieldsInstance(Field field, Object instance) {
		if(instance != null) {
			if(hasField(field, instance)) {
				return instance;
			}
			return getFieldsInstance(field, getEnclosingInstance(instance));
		}
		return null;
	}
	
	private static boolean hasField(Field field, Object instance) {
		return Arrays.stream(instance.getClass().getDeclaredFields())
				.anyMatch(f -> f.equals(field));
	}
	
	private static Object getEnclosingInstance(Object instance) {
		try {
			var enclosing = instance.getClass().getEnclosingClass();
			if(enclosing != null) {
				var field = Arrays.stream(instance.getClass().getDeclaredFields())
						.filter(f -> f.getType().equals(enclosing))
						.findFirst()
						.orElseGet(null);
				if(field != null) {
					field.setAccessible(true);
					return field.get(instance);
				}
			}
		} catch (IllegalAccessException e) { }
		return null;
	}
}
