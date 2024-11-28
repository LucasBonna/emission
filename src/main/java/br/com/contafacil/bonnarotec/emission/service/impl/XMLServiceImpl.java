package br.com.contafacil.bonnarotec.emission.service.impl;

import br.com.contafacil.bonnarotec.emission.domain.exception.XMLProcessingException;
import br.com.contafacil.bonnarotec.emission.domain.xml.XMLProcessResult;
import br.com.contafacil.bonnarotec.emission.service.XMLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
            // Ler o conteúdo original do arquivo preservando caracteres especiais
            String originalXml = new String(file.getBytes(), "UTF-8");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file.getInputStream());
            doc.getDocumentElement().normalize();

            // Procura o valor do ICMS dentro de <total><ICMSTot>
            NodeList totalNodes = doc.getElementsByTagName("total");
            BigDecimal icmsValue = BigDecimal.ZERO;
            boolean icmsFound = false;
            boolean needsUpdate = false;

            for (int i = 0; i < totalNodes.getLength(); i++) {
                Node totalNode = totalNodes.item(i);
                if (totalNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element totalElement = (Element) totalNode;
                    NodeList icmsTotNodes = totalElement.getElementsByTagName("ICMSTot");
                    
                    if (icmsTotNodes.getLength() > 0) {
                        Element icmsTotElement = (Element) icmsTotNodes.item(0);
                        NodeList vICMSUFDestNodes = icmsTotElement.getElementsByTagName("vICMSUFDest");
                        
                        if (vICMSUFDestNodes.getLength() > 0) {
                            String vICMSUFDestValue = vICMSUFDestNodes.item(0).getTextContent();
                            try {
                                icmsValue = new BigDecimal(vICMSUFDestValue);
                                icmsFound = true;
                            } catch (NumberFormatException e) {
                                log.warn("Valor de ICMS inválido encontrado: {}", vICMSUFDestValue);
                                needsUpdate = true;
                            }
                        } else {
                            needsUpdate = true;
                        }
                    }
                }
            }

            // Se não encontrou o valor do ICMS ou o valor é zero, procura em outros lugares
            if (!icmsFound || icmsValue.compareTo(BigDecimal.ZERO) == 0) {
                List<String> icmsTags = new ArrayList<>();
                icmsTags.add("vICMSUFDest");
                icmsTags.add("vICMS");
                icmsTags.add("vICMSST");

                for (String tag : icmsTags) {
                    NodeList nodes = doc.getElementsByTagName(tag);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        String value = nodes.item(i).getTextContent();
                        try {
                            BigDecimal foundValue = new BigDecimal(value);
                            if (foundValue.compareTo(BigDecimal.ZERO) > 0) {
                                icmsValue = foundValue;
                                icmsFound = true;
                                needsUpdate = true;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Valor inválido encontrado para a tag {}: {}", tag, value);
                        }
                    }
                    if (icmsFound) break;
                }
            }

            // Se ainda não encontrou nenhum valor de ICMS
            if (!icmsFound) {
                throw new XMLProcessingException("Não foi possível encontrar um valor de ICMS válido no XML");
            }

            String processedXml = originalXml;
            
            // Só atualiza o XML se for necessário
            if (needsUpdate) {
                // Atualiza o valor do vICMSUFDest dentro de <total><ICMSTot>
                boolean valueUpdated = false;
                for (int i = 0; i < totalNodes.getLength(); i++) {
                    Node totalNode = totalNodes.item(i);
                    if (totalNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element totalElement = (Element) totalNode;
                        NodeList icmsTotNodes = totalElement.getElementsByTagName("ICMSTot");
                        
                        if (icmsTotNodes.getLength() > 0) {
                            Element icmsTotElement = (Element) icmsTotNodes.item(0);
                            NodeList vICMSUFDestNodes = icmsTotElement.getElementsByTagName("vICMSUFDest");
                            
                            if (vICMSUFDestNodes.getLength() > 0) {
                                vICMSUFDestNodes.item(0).setTextContent(icmsValue.toString());
                                valueUpdated = true;
                            } else {
                                // Se não existe a tag vICMSUFDest, cria ela
                                Element vICMSUFDestElement = doc.createElement("vICMSUFDest");
                                vICMSUFDestElement.setTextContent(icmsValue.toString());
                                icmsTotElement.appendChild(vICMSUFDestElement);
                                valueUpdated = true;
                            }
                        }
                    }
                }

                // Se não encontrou a estrutura necessária, cria ela
                if (!valueUpdated) {
                    Element root = doc.getDocumentElement();
                    Element totalElement = doc.createElement("total");
                    Element icmsTotElement = doc.createElement("ICMSTot");
                    Element vICMSUFDestElement = doc.createElement("vICMSUFDest");
                    vICMSUFDestElement.setTextContent(icmsValue.toString());
                    
                    icmsTotElement.appendChild(vICMSUFDestElement);
                    totalElement.appendChild(icmsTotElement);
                    root.appendChild(totalElement);
                }

                // Configura o transformer para preservar caracteres especiais
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                processedXml = writer.getBuffer().toString();
            }

            return new XMLProcessResult(processedXml, icmsValue);
        } catch (XMLProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLProcessingException("Erro ao processar o XML: " + e.getMessage(), e);
        }
    }
}
