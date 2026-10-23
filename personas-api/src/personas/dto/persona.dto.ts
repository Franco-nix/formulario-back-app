import {
  IsEmail,
  IsNotEmpty,
  IsOptional,
  IsString,
  Matches,
  MaxLength,
} from 'class-validator';

/** Campos editables del formulario (el DNI va aparte porque es la PK). */
export class UpdatePersonaDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  nombres: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  apellidos: string;

  @IsOptional()
  @IsString()
  @MaxLength(200)
  direccion?: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(80)
  profesion: string;

  @Matches(/^\d{9}$/, { message: 'celular debe tener 9 dígitos' })
  celular: string;

  @IsEmail()
  @MaxLength(120)
  correo: string;
}

export class CreatePersonaDto extends UpdatePersonaDto {
  @Matches(/^\d{8}$/, { message: 'dni debe tener 8 dígitos' })
  dni: string;
}
