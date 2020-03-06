package uk.chriscormack.netkernel.lighting.desk.model.fixture

class FixtureRegister(val context: FixtureContext) {
    val fixtureList = ArrayList<Fixture>()

    fun register(fixture: Fixture) {
        fixtureList.add(fixture)
    }
}
