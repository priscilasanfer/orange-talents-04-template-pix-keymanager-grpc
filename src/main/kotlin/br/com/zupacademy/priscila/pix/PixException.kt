package br.com.zupacademy.priscila.pix

class ChavePixExistenteException(message: String): RuntimeException(message)

class ChavePixInexistenteException(message: String): RuntimeException(message)

class PermissaoNegadaException (message: String): RuntimeException(message)