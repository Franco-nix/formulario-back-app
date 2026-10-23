import { Column, Entity, PrimaryColumn } from 'typeorm';

@Entity('persona')
export class Persona {
  @PrimaryColumn({ type: 'varchar', length: 8 })
  dni: string;

  @Column({ type: 'varchar', length: 100 })
  nombres: string;

  @Column({ type: 'varchar', length: 100 })
  apellidos: string;

  @Column({ type: 'varchar', length: 200, nullable: true })
  direccion: string | null;

  @Column({ type: 'varchar', length: 80, nullable: true })
  profesion: string | null;

  @Column({ type: 'varchar', length: 15, nullable: true })
  celular: string | null;

  @Column({ type: 'varchar', length: 120, nullable: true })
  correo: string | null;

  // Ruta relativa servida por la API, ej. "uploads/12345678-1700000000000.jpg"
  @Column({ name: 'foto_ruta', type: 'text', nullable: true })
  fotoRuta: string | null;
}
