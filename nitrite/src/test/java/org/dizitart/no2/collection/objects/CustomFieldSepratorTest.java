package org.dizitart.no2.collection.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.objects.data.Company;
import org.dizitart.no2.collection.objects.data.Note;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;

import static org.dizitart.no2.filters.Filter.eq;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class CustomFieldSepratorTest {
    private ObjectRepository<EmployeeForCustomSeparator> repository;

    @Before
    public void setUp() {
        Nitrite db = Nitrite.builder()
                .fieldSeparator(":")
                .openOrCreate();
        repository = db.getRepository(EmployeeForCustomSeparator.class);
    }

    @Test
    public void testFieldSeparator() {
        assertEquals(NitriteContext.getFieldSeparator(), ":");
    }

    @Test
    public void testFindByEmbeddedField() {
        EmployeeForCustomSeparator employee = new EmployeeForCustomSeparator();
        employee.setCompany(new Company());
        employee.setEmployeeNote(new Note());

        employee.setEmpId(123L);
        employee.setJoinDate(new Date());
        employee.setBlob(new byte[0]);
        employee.setAddress("Dummy address");

        employee.getCompany().setCompanyId(987L);
        employee.getCompany().setCompanyName("Dummy Company");
        employee.getCompany().setDateCreated(new Date());

        employee.getEmployeeNote().setNoteId(567L);
        employee.getEmployeeNote().setText("Dummy Note");

        repository.insert(employee);

        assertEquals(repository.find(eq("employeeNote.text", "Dummy Note")).size(), 0);
        assertEquals(repository.find(eq("employeeNote:text", "Dummy Note")).size(), 1);

        assertEquals(repository.find(eq("company.companyName", "Dummy Company")).size(), 0);
        assertEquals(repository.find(eq("company:companyName", "Dummy Company")).size(), 1);
    }

    @ToString
    @EqualsAndHashCode
    @Indices({
            @Index(value = "joinDate", type = IndexType.NonUnique),
            @Index(value = "address", type = IndexType.Fulltext),
            @Index(value = "employeeNote:text", type = IndexType.Fulltext)
    })
    public static class EmployeeForCustomSeparator implements Serializable {
        @Id
        @Getter
        @Setter
        private Long empId;

        @Getter
        @Setter
        private Date joinDate;

        @Getter
        @Setter
        private String address;

        @Getter
        @Setter
        private Company company;

        @Getter
        @Setter
        private byte[] blob;

        @Getter
        @Setter
        private Note employeeNote;

        EmployeeForCustomSeparator() {}

        public EmployeeForCustomSeparator(EmployeeForCustomSeparator copy) {
            empId = copy.empId;
            joinDate = copy.joinDate;
            address = copy.address;
            company = copy.company;
            blob = copy.blob;
            employeeNote = copy.employeeNote;
        }
    }

}
