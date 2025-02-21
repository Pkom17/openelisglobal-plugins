package oe.plugin.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.analyzerresults.valueholder.AnalyzerResults;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.plugin.ServletPlugin;
import org.openelisglobal.sample.service.SampleService;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.spring.util.SpringContext;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.custom.factory.PluginModelClassFactory;
import ca.uhn.hl7v2.custom.model.v25.group.RSP_Z02_ORDER;
import ca.uhn.hl7v2.custom.model.v25.group.RSP_Z02_PATIENT;
import ca.uhn.hl7v2.custom.model.v25.message.QBP_Z03;
import ca.uhn.hl7v2.custom.model.v25.message.RSP_Z02;
import ca.uhn.hl7v2.hoh.hapi.server.HohServlet;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.message.QCN_J01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.NTE;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.QAK;
import ca.uhn.hl7v2.model.v25.segment.QPD;
import ca.uhn.hl7v2.model.v25.segment.SPM;
import ca.uhn.hl7v2.model.v25.segment.TQ1;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

@SuppressWarnings("serial")
public class HttpHl7TemplateAnalyzerServlet extends HohServlet implements ServletPlugin {

	SampleService sampleService = SpringContext.getBean(SampleService.class);
	AnalysisService analysisService = SpringContext.getBean(AnalysisService.class);
	private HttpHl7TemplateAnalyzerImplementation inserter = new HttpHl7TemplateAnalyzerImplementation();

	@Override
	public void init(ServletConfig theConfig) throws ServletException {
		ReceivingApplication<Message> application = new HttpHl7TemplateAnalyzerApplication();
		setApplication(application);
	}

	/**
	 * The application does the actual processing
	 */
	private class HttpHl7TemplateAnalyzerApplication implements ReceivingApplication<Message> {

		private Map<String, Message> activeRequests = new HashMap<>();

		/**
		 * processMessage is fired each time a new message arrives.
		 *
		 * @param message     The message which was received
		 * @param theMetadata A map containing additional information about the message,
		 *                    where it came from, etc.
		 */
		@Override
		public Message processMessage(Message message, @SuppressWarnings("rawtypes") Map theMetadata)
				throws ReceivingApplicationException, HL7Exception {
			MSH messageHeader = (MSH) message.get("MSH");

			Message response = null;
			try {
				switch (messageHeader.getMessageType().getMessageStructure().getValueOrEmpty()) {
				default:
					response = message.generateACK(AcknowledgmentCode.AR,
							new HL7Exception("not configured to deal with request"));
				}
			} catch (IOException e) {
				throw new ReceivingApplicationException(e);
			}
			return response;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean canProcess(Message theMessage) {
			return true;
		}

	}
}
