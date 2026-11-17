package br.com.badgr.mobile.data.local

import br.com.badgr.mobile.domain.model.Event

/** Agenda fictícia. Coordenadas de referência, sem parceria ou evento confirmado. */
object DemoEvents {
    val all = listOf(
        Event(1,"UMBRA","21 NOV · SÁB, 22H","Espaço Aurora","São Paulo, SP","Techno",3500,100,-23.5505,-46.6333,"Uma noite de pistas escuras, encontros e música eletrônica independente. Line-up de demonstração: Luma, Nox e Vértice. Classificação 18 anos. Evento fictício para o trabalho acadêmico.",0xFF8655E9.toInt()),
        Event(2,"CORPO LIVRE","28 NOV · SÁB, 23H","Casa Prisma","São Paulo, SP","House",2500,80,-23.5575,-46.6559,"House, expressão e liberdade na pista. Uma experiência de demonstração inspirada na cena independente LGBTQIA+. Classificação 18 anos. Evento fictício.",0xFFE955AA.toInt()),
        Event(3,"GRAVE ABERTO","05 DEZ · SÁB, 18H","Galpão Frequência","São Paulo, SP","Bass",0,60,-23.5417,-46.6400,"Cultura de sound system, graves e encontro de coletivos. Entrada gratuita com reserva local. Classificação 18 anos. Evento fictício para demonstração.",0xFF55B7A8.toInt()),
        Event(4,"ENTRE FREQUÊNCIAS","12 DEZ · SÁB, 21H","Estúdio Subsolo","São Paulo, SP","Experimental",1000,50,-23.5680,-46.6430,"Escuta, performance e experimentação sonora em uma noite dedicada a artistas independentes. Classificação 18 anos. Evento fictício.",0xFFD58C4E.toInt())
    )
}
