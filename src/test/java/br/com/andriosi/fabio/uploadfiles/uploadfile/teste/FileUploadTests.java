package br.com.andriosi.fabio.uploadfiles.uploadfile.teste;

import br.com.andriosi.fabio.uploadfiles.storage.StorageService;
import br.com.andriosi.fabio.uploadfiles.storage.StorageFileNotFoundException;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {
    @Autowired
    private MockMvc mmvc;
    @MockBean
    private StorageService storageService;

    @Test
    public void shouldListAllFiles() throws Exception{
        given(this.storageService.loadAll()).willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mmvc.perform(get("/")).andExpect(status().isOk()).andExpect(model().attribute("files",
                Matchers.contains("http://localhost/files/first.txt", "http://localhost/files/second.txt")));
    }

    @Test
    public void shouldSaveUploadFiles() throws Exception{
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file","text.txt","text/plain",
                "Spring Framework".getBytes());
        this.mmvc.perform(fileUpload("/").file(mockMultipartFile))
                .andExpect(status().isFound()).andExpect(header().string("Location","/"));
        then(this.storageService).should().store(mockMultipartFile);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void should404WhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(StorageFileNotFoundException.class);

        this.mmvc.perform(get("/files/test.txt")).andExpect(status().isNotFound());
    }
}
