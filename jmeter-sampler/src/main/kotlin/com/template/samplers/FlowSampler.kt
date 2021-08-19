package com.template.samplers

import com.r3.corda.jmeter.AbstractSampler
import com.template.flows.Initiator
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import org.apache.jmeter.config.Argument
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext
import org.apache.jmeter.samplers.SampleResult

/**
 * A JMeter sampler class that invokes the Initiator flow.
 *
 * Note that it derives from AbstractSampler - this is a base class that provides the basic JMeter integration and a few
 * helper methods that are required to run a CorDapp flow.
 * Anything that is specific to the CorDapp or the flow should go into the derived class.
 */
class FlowSampler : AbstractSampler() {
    companion object JMeterProperties {
        val otherParty = Argument("otherPartyName", "", "<meta>", "The X500 name of the receiver.")
    }

    lateinit var counterParty: Party

    /**
     * This set-up method is called once before the tests start and can be used to initialise values that will be required
     * for the tests.
     */
    override fun setupTest(rpcProxy: CordaRPCOps, testContext: JavaSamplerContext) {
        // this method initialises the notary field on the base class - not all samplers might need a notary, so it has
        // to be called explicitly if you want to use the notary field.
        getNotaryIdentity(rpcProxy, testContext)

        // This is a generic helper to turn an X500 name into a Corda identity via RPC
        counterParty = getIdentity(rpcProxy, testContext, otherParty)
    }

    /**
     * This method gets called to create the flow invoke to run the CorDapp - this method gets called for every run of
     * the sampler - the flow invoke class is then used to invoke the flow via RPC and time how long it takes to complete.
     */
    override fun createFlowInvoke(rpcProxy: CordaRPCOps, testContext: JavaSamplerContext): FlowInvoke<*> {
        return FlowInvoke<Initiator>(Initiator::class.java, arrayOf(
            counterParty
        ))
    }

    /**
     * Any clean-up task that needs doing should go here - beware that this is only run after all tests in one testplan
     * are done, so this is no good to release resources that might be required by subsequent tests.
     */
    override fun teardownTest(rpcProxy: CordaRPCOps, testContext: JavaSamplerContext) {
    }

    /**
     * This is the list of arguments that we expect to get from the testplan - anything returned here appears as a
     * parameter in the testplan and can be configured on a per test basis. Note that the notary Argument is defined
     * on the AbstractSampler class, but still needs to be included here if we plan to use a notary.
     */
    override val additionalArgs: Set<Argument>
        get() = setOf(AbstractSampler.notary, otherParty)

    /**
     * This method gets invoked after each sample result has been collected and allows to add extra information to the
     * sample. Overriding it is optional, by default it is a no op.
     */
    override fun additionalFlowResponseProcessing(context: JavaSamplerContext, sample: SampleResult, response: Any?) {
        // Optionally add data from the response to the sample (e.g. performance figures the flow has collected)
    }
}