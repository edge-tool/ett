package gov.nist.healthcare.ttt.xdr.domain
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
/**
 * Created by gerardin on 10/15/14.
 */

@XmlRootElement(name = "report")
@XmlAccessorType(value=XmlAccessType.FIELD)
class TkValidationReport {

    @XmlElement
    String status

    @XmlElement
    String simId

    @XmlElement
    String endpoint

    @XmlElement
    String endpointTLS




}
