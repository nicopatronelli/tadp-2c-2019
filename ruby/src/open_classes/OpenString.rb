class String
  def attr_to_sym # ":@var" -> :var
    self.gsub("@", "")
  end
end