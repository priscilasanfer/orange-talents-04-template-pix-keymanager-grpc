package br.com.zupacademy.priscila

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.priscila")
		.start()
}

