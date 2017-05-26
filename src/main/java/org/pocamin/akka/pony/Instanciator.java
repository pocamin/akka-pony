package org.pocamin.akka.pony;

/**

 */
@FunctionalInterface
public interface Instanciator<T> {

	T newInstance(Class<T> clazz) throws IllegalAccessException, InstantiationException;

}
