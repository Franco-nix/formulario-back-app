import {
  BadRequestException,
  Body,
  Controller,
  Get,
  Param,
  ParseFilePipe,
  Post,
  Put,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { diskStorage } from 'multer';
import { mkdirSync } from 'node:fs';
import { unlink } from 'node:fs/promises';
import { extname, join } from 'node:path';
import { CreatePersonaDto, UpdatePersonaDto } from './dto/persona.dto.js';
import { Persona } from './persona.entity.js';
import { PersonasService } from './personas.service.js';

export const UPLOADS_DIR = join(process.cwd(), 'uploads');
mkdirSync(UPLOADS_DIR, { recursive: true });

function validarDni(dni: string): string {
  if (!/^\d{8}$/.test(dni))
    throw new BadRequestException('dni debe tener 8 dígitos');
  return dni;
}

@Controller('personas')
export class PersonasController {
  constructor(private readonly personas: PersonasService) {}

  @Get(':dni')
  buscar(@Param('dni') dni: string): Promise<Persona> {
    return this.personas.buscar(validarDni(dni));
  }

  @Post()
  crear(@Body() dto: CreatePersonaDto): Promise<Persona> {
    return this.personas.crear(dto);
  }

  @Put(':dni')
  actualizar(
    @Param('dni') dni: string,
    @Body() dto: UpdatePersonaDto,
  ): Promise<Persona> {
    return this.personas.actualizar(validarDni(dni), dto);
  }

  @Post(':dni/foto')
  @UseInterceptors(
    FileInterceptor('foto', {
      storage: diskStorage({
        destination: UPLOADS_DIR,
        filename: (req, file, cb) =>
          cb(
            null,
            `${req.params.dni}-${Date.now()}${extname(file.originalname) || '.jpg'}`,
          ),
      }),
      fileFilter: (req, file, cb) =>
        file.mimetype.startsWith('image/')
          ? cb(null, true)
          : cb(new BadRequestException('La foto debe ser una imagen'), false),
      limits: { fileSize: 10 * 1024 * 1024 },
    }),
  )
  async subirFoto(
    @Param('dni') dni: string,
    @UploadedFile(new ParseFilePipe())
    foto: Express.Multer.File,
  ): Promise<Persona> {
    try {
      return await this.personas.guardarFoto(validarDni(dni), foto.filename);
    } catch (e) {
      await unlink(foto.path).catch(() => undefined);
      throw e;
    }
  }
}
