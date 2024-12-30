package br.com.contafacil.bonnarotec.emission.service.impl;

import br.com.contafacil.bonnarotec.emission.domain.exception.XMLProcessingException;
import br.com.contafacil.bonnarotec.emission.domain.xml.XMLProcessResult;
import br.com.contafacil.bonnarotec.emission.service.XMLService;
import br.com.contafacil.bonnarotec.emission.util.MultipartFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.transform.Transformer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.math.BigDecimal;

@Slf4j
@Service
public class XMLServiceImpl implements XMLService {

    @Override
    public boolean isValidXml(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(file.getInputStream());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public XMLProcessResult validateAndProcessGNREXml(MultipartFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);  // Enable namespace awareness
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file.getInputStream());

            String NAMESPACE_URI = "http://www.portalfiscal.inf.br/nfe";

            // Extrair o numero da NFe
            NodeList cNFList = doc.getElementsByTagName("cNF");
            if (cNFList.getLength() == 0) {
                throw new XMLProcessingException("Tag cNF não encontrada no XML");
            }
            Element cNF = (Element) cNFList.item(0);
            String numNota = cNF.getTextContent();

            // Extrair o xNome dentro de <dest>
            NodeList destList = doc.getElementsByTagNameNS(NAMESPACE_URI, "dest");
            if (destList.getLength() == 0) {
                throw new XMLProcessingException("Tag dest não encontrada no XML");
            }
            Element dest = (Element) destList.item(0);
            NodeList xDestList = dest.getElementsByTagNameNS(NAMESPACE_URI, "xNome");
            if (xDestList.getLength() == 0) {
                throw new XMLProcessingException("Tag xNome não encontrada dentro de dest no XML");
            }
            Element xDest = (Element) xDestList.item(0);
            String xDestValue = xDest.getTextContent();

            // Extrair a chave da NFe
            NodeList chNFeList = doc.getElementsByTagName("chNFe");
            if (chNFeList.getLength() == 0) {
                throw new XMLProcessingException("Tag chNFe não encontrada no XML");
            }
            Element chNFe = (Element) chNFeList.item(0);
            String chaveNota = chNFe.getTextContent();

            // Buscar o valor de vICMSUFDest dentro de ICMSTot
            NodeList icmsTotList = doc.getElementsByTagName("ICMSTot");
            if (icmsTotList.getLength() == 0) {
                throw new XMLProcessingException("Tag ICMSTot não encontrada no XML");
            }
            Element icmsTot = (Element) icmsTotList.item(0);
            NodeList vICMSUFDestList = icmsTot.getElementsByTagName("vICMSUFDest");
            if (vICMSUFDestList.getLength() == 0) {
                throw new XMLProcessingException("Tag vICMSUFDest não encontrada no XML");
            }
            Element vICMSUFDestElement = (Element) vICMSUFDestList.item(0);
            String vICMSUFDestValue = vICMSUFDestElement.getTextContent();
            BigDecimal icmsValue = new BigDecimal(vICMSUFDestValue);

            MultipartFile processedXml = file;  // Default to original file if no changes needed

            // Se vICMSUFDest for 0, buscar valor nos impostos estaduais
            if (BigDecimal.ZERO.compareTo(icmsValue) == 0) {
                NodeList infAdicList = doc.getElementsByTagName("infCpl");
                if (infAdicList.getLength() > 0) {
                    String infCplContent = infAdicList.item(0).getTextContent();
                    // Buscar o valor dos impostos estaduais
                    int estaduaisIndex = infCplContent.indexOf("Estaduais R$");
                    if (estaduaisIndex != -1) {
                        // Extrair o valor após "Estaduais R$"
                        String subStr = infCplContent.substring(estaduaisIndex + 12);
                        // Encontrar o valor usando regex
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+[,.]\\d+");
                        java.util.regex.Matcher matcher = pattern.matcher(subStr);
                        if (matcher.find()) {
                            String estaduaisValue = matcher.group().replace(",", ".");
                            icmsValue = new BigDecimal(estaduaisValue);
                            
                            // Atualizar apenas o vICMSUFDest dentro de ICMSTot
                            vICMSUFDestElement.setTextContent(estaduaisValue);
                            
                            // Configurar a transformação preservando a estrutura exata
                            TransformerFactory transformerFactory = TransformerFactory.newInstance();
                            Transformer transformer = transformerFactory.newTransformer();
                            
                            // Manter as configurações originais do XML sem adicionar atributos extras
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");  // Não adicionar declaração XML
                            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                            transformer.setOutputProperty(OutputKeys.INDENT, "no");  // Não adicionar indentação
                            
                            StringWriter writer = new StringWriter();
                            transformer.transform(new DOMSource(doc), new StreamResult(writer));
                            
                            // Criar novo MultipartFile mantendo a estrutura original
                            processedXml = MultipartFileUtil.convertStringToMultipartFile(
                                writer.toString(),
                                file.getOriginalFilename()
                            );
                        }
                    }
                }
            } else {
               processedXml = null; 
            }

            return new XMLProcessResult(processedXml, icmsValue, chaveNota, numNota, xDestValue);

        } catch (Exception e) {
            log.error("Erro ao processar XML GNRE: ", e);
            throw new XMLProcessingException("Erro ao processar XML GNRE: " + e.getMessage());
        } 
    }
}