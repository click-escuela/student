package click.escuela.student.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import click.escuela.student.api.AdressApi;
import click.escuela.student.api.ParentApi;
import click.escuela.student.api.StudentApi;
import click.escuela.student.enumerator.GenderType;
import click.escuela.student.enumerator.StudentEnum;
import click.escuela.student.exception.TransactionException;
import click.escuela.student.mapper.Mapper;
import click.escuela.student.model.Adress;
import click.escuela.student.model.Parent;
import click.escuela.student.model.Student;
import click.escuela.student.repository.StudentRepository;
import click.escuela.student.service.impl.StudentServiceImpl;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Mapper.class})
public class StudentServiceTest{
	
	@Mock
	private StudentRepository studentRepository;
	private StudentServiceImpl studentService = new StudentServiceImpl();
	//private StudentApi studentApi;
	

	private StudentServiceImpl studentServiceImpl = new StudentServiceImpl();
	private StudentApi studentApi;
	private UUID id;
	
	@Before
	public void setUp() {
 		PowerMockito.mockStatic(Mapper.class);
		

		Student student = Student.builder().absences(3).birthday(LocalDate.now()).cellPhone("535435").
				document("342343232").division("B").grade("2°").email("oscar@gmail.com").gender(GenderType.MALE).name("oscar").parent(new Parent()).build();
		
		ParentApi parentApi = new ParentApi();
		parentApi.setAdressApi(new AdressApi());
		
		studentApi = StudentApi.builder().adressApi(new AdressApi()).birthday(LocalDate.now())
				.cellPhone("4534543").division("C").grade("3°").document("435345").email("oscar@gmail.com").gender(GenderType.MALE.toString())
				.name("oscar").parentApi(parentApi).schoolId(1234).build();
		Optional<Student> optional = Optional.of(student);

		Mockito.when(Mapper.mapperToAdress(Mockito.any())).thenReturn(new Adress());
		Mockito.when(Mapper.mapperToParent(Mockito.any())).thenReturn(new Parent());
 		Mockito.when(Mapper.mapperToStudent(studentApi)).thenReturn(student);
 		id = UUID.randomUUID();
 		Mockito.when(studentRepository.save(student)).thenReturn(student);	
 		Mockito.when(studentRepository.findById(id)).thenReturn(optional);		

 		//Mockito.when(clientRepository.findById(1L)).thenReturn(optional);

		//Mockito.when(clientRepository.findAll()).thenReturn(Arrays.asList(new Client(1L,"23423432","oscar",LocalDateTime.of(2020, 10, 20, 12, 12))));

		//Mockito.doNothing().when(clientRepository).delete(Mockito.any());
		
		//inyecta en el servicio el objeto repository
		ReflectionTestUtils.setField(studentServiceImpl, "studentRepository", studentRepository);
	}
	
	@Test
	public void CreateStudent() throws TransactionException {
		StudentApi studentTest=new StudentApi();	
		studentTest.setName("Walter");
		studentTest.setSchoolId(1234);
		
		studentService.create(studentTest);
		
		List<Student> studentTestList=studentRepository.findAll();
		
		Mockito.when(studentRepository.findBySchoolId(1234)).thenReturn(studentTestList);
		
		assertEquals("Walter",studentTestList.get(2).getName());
	
	}
	
	@Test
	public void whenCreateIsOk() {
		boolean hasError = false;
		try {
			studentServiceImpl.create(studentApi);
		} catch (Exception e) {
			hasError = true;
		}
		assertThat(hasError).isFalse();
	}
	
	@Test
	public void whenUpdateOk() {
		boolean hasError = false;
		try {
			studentServiceImpl.update(studentApi);
		} catch (Exception e) {
			hasError = true;
		}
		assertThat(hasError).isFalse();
	}
	
	@Test
	public void whenUpdateIsError() {
		
		id = UUID.randomUUID();
		assertThatExceptionOfType(TransactionException.class)
		  .isThrownBy(() -> {

				studentServiceImpl.update(studentApi);
		}).withMessage(StudentEnum.UPDATE_ERROR.getDescription());

	}
	@Test
	public void whenCreateIsError() {

		StudentApi studentApi = StudentApi.builder().adressApi(new AdressApi()).birthday(LocalDate.now())
				.cellPhone("4534543").document("55555").division("F").grade("3°").email("oscar@gmail.com").gender(GenderType.MALE.toString())
				.name("oscar").parentApi(new ParentApi()).schoolId(1234).build();
		
		Mockito.when(studentRepository.save(null)).thenThrow(IllegalArgumentException.class);
		
		assertThatExceptionOfType(TransactionException.class)
		  .isThrownBy(() -> {

				studentServiceImpl.create(studentApi);
		}).withMessage("No se pudo crear el estudiante correctamente");

	}
	
	
	
}