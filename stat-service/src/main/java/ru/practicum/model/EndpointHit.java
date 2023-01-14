package ru.practicum.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hit")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "endpoint_hit_id")
    private Long id;

    @NonNull
    @Column(name = "app", nullable = false, length = 150)
    private String app;

    @NonNull
    @Column(name = "uri", nullable = false, length = 150)
    private String uri;

    @NonNull
    @Column(name = "ip", nullable = false, length = 30)
    private String ip;

    private LocalDateTime timestamp;

}
