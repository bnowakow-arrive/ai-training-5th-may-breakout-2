package com.arrive.ai_training_5th_may_breakout_2.ui

import com.arrive.ai_training_5th_may_breakout_2.contracts.CreateCompetitorRequest
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField

class AddCompetitorDialog(
	private val onSave: (CreateCompetitorRequest) -> Unit,
) : Dialog() {

	private val nameField = TextField("Name").apply {
		isRequired = true
		setWidthFull()
	}
	private val domainField = TextField("Domain").apply {
		isRequired = true
		placeholder = "example.com"
		setWidthFull()
	}
	private val isOwnField = Checkbox("This is us (Arrive)")

	init {
		headerTitle = "Add competitor"
		isCloseOnOutsideClick = false
		add(
			VerticalLayout(nameField, domainField, isOwnField).apply {
				setPadding(false)
				setSpacing(true)
			},
		)
		footer.add(
			Button("Cancel") { close() },
			Button("Save") { save() }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) },
		)
	}

	private fun save() {
		val name = nameField.value?.trim().orEmpty()
		val domain = domainField.value?.trim()?.lowercase().orEmpty()
		if (name.isEmpty() || domain.isEmpty()) {
			Notification.show("Name and domain are required")
			return
		}
		close()
		onSave(CreateCompetitorRequest(name = name, domain = domain, isOwn = isOwnField.value))
	}
}
