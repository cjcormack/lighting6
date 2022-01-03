package uk.chriscormack.netkernel.lighting.desk.model.fixture

import kotlin.reflect.KClass
import kotlin.reflect.KType

abstract class Fixture(val key: String, val fixtureName: String, val position: Int) {
    val fixtureProperties: List<FixtureProperty> = this::class.supertypes.fixturePropertyAnnotations

    private val List<KType>.fixturePropertyAnnotations: List<FixtureProperty>
        get() {
            val lightProperties = ArrayList<FixtureProperty>()

            this.forEach { classifier ->
                val classifier = classifier.classifier

                if (classifier is KClass<*>) {
                    lightProperties.addAll(classifier.annotations.mapNotNull { annotation ->
                        if (annotation is FixtureProperty) {
                            annotation
                        } else {
                            null
                        }
                    })
                }
            }

            return lightProperties
        }
}
