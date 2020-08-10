package pt.arquivo.indexer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.tika.exception.TikaException;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import pt.arquivo.indexer.data.PageData;
import pt.arquivo.indexer.parsers.WARCParser;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class WARCParserTest {

    private WARCParser warcParser;

    @Before
    public void init() {
        Config conf = ConfigFactory.load();
        this.warcParser = new WARCParser(conf);
    }

    private PageData getPageDataDocFromRecord(URL warcTest) throws SAXException, TikaException, NoSuchAlgorithmException, IOException {
        ArchiveReader reader = ArchiveReaderFactory.get(warcTest);
        Iterator<ArchiveRecord> ir = reader.iterator();
        ArchiveRecord rec = ir.next();
        return warcParser.extract(reader.getFileName(), rec);
    }

    @Test
    public void extract() throws IOException, NoSuchAlgorithmException, TikaException, SAXException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL fileUrl = classLoader.getResource("chunked.gzip.html.record.warc.gz");

        PageData doc = getPageDataDocFromRecord(fileUrl);

        assertThat(doc.getTitle()).isEqualTo("");
        assertThat(doc.getContent()).contains("Forbidden");
        assertThat(doc.getCollection()).isEqualTo("");
        assertThat(doc.getAnchor()).isEqualTo("");
        assertThat(doc.getPrimaryType()).isEqualTo("text");
        assertThat(doc.getSubType()).isEqualTo("html");
        assertThat(doc.getType()).isEqualTo("text/html");
        assertThat(doc.getUrl()).isEqualTo("http://publico.pt/");
        assertThat(doc.getSurt_url()).isEqualTo("(pt,publico,");
        assertThat(doc.getnOutLinks()).isEqualTo(2);
        assertThat(doc.getId()).isEqualTo("19961013180344/FhcAj9+g5IrKjL+HJyyf3g==");
        assertThat(doc.getSite()).isEqualTo("publico.pt");
        assertThat(doc.getTstamp()).isEqualTo("19961013180344");
        assertThat(doc.getContentLength()).isEqualTo(265);
        assertThat(doc.getWarcName()).isEqualTo("chunked.gzip.html.record.warc.gz");
        assertThat(doc.getWarcOffset()).isEqualTo(0);

        // Test warc record with problematic encoding
        // Test only fields with text
        fileUrl = classLoader.getResource("problematic_encoding_record.warc.gz");
        doc = getPageDataDocFromRecord(fileUrl);

        // TODO fix this problem using a better CharDetector than Tika (or just wait to Tika improve the one it uses)
        assertThat(doc.getTitle()).isEqualTo("DicionÃ¡rio Priberam da LÃ\u00ADngua Portuguesa");
        assertThat(doc.getContent()).isEqualTo("O meu perfil |  FormaÃ§Ã£o e Eventos |  Suporte |  Contactos |  Procurar |  DicionÃ¡rio |  FLiP.pt |  LegiX.pt |  Blogue EMPRESA Ã�REAS PRODUTOS LOJA    DicionÃ¡rio Priberam da LÃ\u00ADngua Portuguesa   PÃ¡gina Principal | Sobre o dicionÃ¡rio | Como consultar | Abreviaturas | GramÃ¡tica | Downloads | LigaÃ§Ãµes Ãšteis Acordo OrtogrÃ¡fico: Antes Depois Pesquisar nas definiÃ§Ãµes querer | v. tr. | v. intr. | v. pron. | s. m. querer (Ãª) - Conjugar v. tr. 1. Ter a vontade ou a intenÃ§Ã£o de. 2. Anuir ao desejo de outrem. 3. Ordenar, exigir. 4. Procurar. 5. Poder (falando de coisas). 6. Requerer, ter necessidade de. 7. Fazer o possÃ\u00ADvel para, dar motivos para. 8. Permitir, tolerar (principalmente quando acompanhado de negaÃ§Ã£o). 9. Admitir, supor. v. intr. 10. Exprimir terminantemente a vontade. 11. Amar, estimar. v. pron. 12. Desejar estar, desejar ver-se. 13. Amar-se. s. m. 14. Desejo, vontade. Queira Deus!:Â designativa de ameaÃ§a ou intimaÃ§Ã£o para que alguÃ©m nÃ£o pratique qualquer acto!ato. ExpressÃ£o que traduz um desejo:Â uma ansiedade, uma sÃºplica. Querer bem:Â amar. Querer mal:Â odiar. Queira (seguido de verbo no infinito):Â faÃ§a o favor de. Sem querer:Â nÃ£o de propÃ³sito. Palavras relacionadas com: querer Esta palavra foi consultada: NÃ£o foram efectuadas quaisquer pesquisas para essa palavra. nos Ãºltimos 103 dias Ãšltimas pesquisas: Palavra do dia braquigrafia (grego brachÃºs, -eÃ®a, -Ãº, curto + grego graphÃ©, -Ãªs, escrita + -ia) s. f. Arte de escrever por abreviaturas. Ver mais... DÃºvidas LinguÃ\u00ADsticas Castanheira de PÃªra e outros topÃ³nimos depois do Acordo OrtogrÃ¡fico A palavra PÃªra do nome de localidades como Castanheira de PÃªra perde o acento com o novo Acordo OrtogrÃ¡fico? grafia de BielorrÃºssia Qual Ã© a ortografia correcta para esta antiga repÃºblica soviÃ©tica: BielorÃºssia, BielorrÃºssia ou BielorÃºsia? re-e... ou ree... com o novo Acordo OrtogrÃ¡fico? Ao consultar algumas palavras no DicionÃ¡rio Priberam, deparei-me com alguns erros. Embora considere o dicionÃ¡rio muito Ãºtil, notei que pala... Ver mais... Â© 2009 Priberam InformÃ¡tica, S.A. Todos os direitos reservados | InformaÃ§Ã£o legal | Sugerir palavra Site by Evidensys Enviar... De Receber cÃ³pia da mensagem Nome Email Para Nome Email Mensagem Como fazer uma ligaÃ§Ã£o para este verbete: Como citar este verbete: \"querer\", in DicionÃ¡rio Priberam da LÃ\u00ADngua Portuguesa [em linha], 2009, http://www.priberam.pt/dlpo/dlpo.aspx?pal=querer [consultado em 25-06-2009].");
        assertThat(doc.getContentLength()).isEqualTo(59868);
    }
}